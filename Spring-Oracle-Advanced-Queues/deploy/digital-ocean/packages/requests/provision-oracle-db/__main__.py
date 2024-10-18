import requests
import time
import os
import json

BASE_URL = "https://api.digitalocean.com/v2"


def handle_response(status, body):
    """
    Returns a response to the caller as a dictionary.

    Args:
        status (int): The HTTP status code to return.
        body (dict): The body of the response.

    Returns:
        dict: A dictionary containing the HTTP status code and the JSON-encoded body.
    """
    print(status, body)
    return {
        "statusCode": status,
        "body": json.dumps(body)
    }


def get_digitalocean_token(args):
    """
    Retrieve DigitalOcean API token from arguments or environment.

    Args:
        args (dict): The input arguments provided to the function.

    Returns:
        str: The DigitalOcean API token.

    Raises:
        ValueError: If the API token is not provided.
    """
    token = args.get("DIGITALOCEAN_TOKEN") or os.environ.get("DIGITALOCEAN_TOKEN")
    if not token:
        raise ValueError("API token is missing.")
    return token


def droplet_exists(droplet_name, headers):
    """
    Check if a droplet with the given name exists.

    Args:
        droplet_name (str): The name of the droplet to check.
        headers (dict): The request headers.

    Returns:
        tuple: A tuple where the first element is a boolean indicating whether the droplet exists,
               and the second element is the ID of the droplet if it exists, or None.
    """
    response = requests.get(f"{BASE_URL}/droplets", headers=headers)
    response.raise_for_status()
    droplets = response.json()["droplets"]

    for droplet in droplets:
        if droplet["name"] == droplet_name:
            return True, droplet["id"]
    return False, None


def get_snapshot_id(snapshot_name, headers):
    """
    Get the ID of a snapshot by its name.

    Args:
        snapshot_name (str): The name of the snapshot to look up.
        headers (dict): The request headers.

    Returns:
        str: The ID of the snapshot.

    Raises:
        Exception: If the snapshot is not found.
    """
    response = requests.get(f"{BASE_URL}/snapshots", headers=headers)
    response.raise_for_status()
    snapshots = response.json()["snapshots"]

    for snapshot in snapshots:
        if snapshot["name"] == snapshot_name:
            return snapshot["id"]
    raise Exception(f"Snapshot {snapshot_name} not found.")


def get_ssh_key_id(ssh_key_name, headers):
    """
    Get the ID of an SSH key by its name.

    Args:
        ssh_key_name (str): The name of the SSH key to look up.
        headers (dict): The request headers.

    Returns:
        str: The ID of the SSH key.

    Raises:
        Exception: If the SSH key is not found.
    """
    response = requests.get(f"{BASE_URL}/account/keys", headers=headers)
    response.raise_for_status()
    keys = response.json()["ssh_keys"]

    for key in keys:
        if key["name"] == ssh_key_name:
            return key["id"]
    raise Exception(f"SSH key {ssh_key_name} not found.")


def create_droplet(droplet_name, snapshot_id, ssh_key_id, tag_name, region="blr1", size="s-1vcpu-1gb-amd",
                   headers=None):
    """
    Create a new DigitalOcean droplet.

    Args:
        droplet_name (str): The name of the droplet to create.
        snapshot_id (str): The ID of the snapshot to use as the base image.
        ssh_key_id (str): The ID of the SSH key to install on the droplet.
        tag_name (str): The name of the tag to assign to the droplet.
        region (str, optional): The region to create the droplet in. Defaults to "blr1".
        size (str, optional): The size of the droplet to create. Defaults to "s-1vcpu-1gb".
        headers (dict): The request headers.

    Returns:
        str: The ID of the newly created droplet.
    """
    payload = {
        "name": droplet_name,
        "region": region,
        "size": size,
        "image": snapshot_id,
        "ssh_keys": [ssh_key_id],
        "ipv6": True,
        "tags": [tag_name]
    }

    response = requests.post(f"{BASE_URL}/droplets", json=payload, headers=headers)
    response.raise_for_status()
    return response.json()["droplet"]["id"]


def get_droplet_ip(droplet_id, headers, max_retries=5):
    """
    Poll the droplet status and wait for the IP address to be assigned.

    Args:
        droplet_id (str): The ID of the droplet to check.
        headers (dict): The request headers.
        max_retries (int, optional): The maximum number of retries. Defaults to 5.

    Returns:
        str: The assigned IPv4 address of the droplet.

    Raises:
        TimeoutError: If the IP address is not assigned within the allowed retries.
    """
    for _ in range(max_retries):
        response = requests.get(f"{BASE_URL}/droplets/{droplet_id}", headers=headers)
        response.raise_for_status()
        droplet = response.json()["droplet"]
        v4_ip = next((net["ip_address"] for net in droplet["networks"]["v4"] if net["type"] == "public"), None)
        v6_ip = next((net["ip_address"] for net in droplet["networks"]["v6"] if net["type"] == "public"), None)
        if v4_ip:
            print(f"IPv4: {v4_ip}")
            return v4_ip
        if v6_ip:
            print(f"IPv6: {v6_ip}")
            return v6_ip
        print("Waiting for the droplet to be fully provisioned...")
        time.sleep(10)
    raise TimeoutError("Droplet IP assignment timed out.")


def ensure_a_record(domain_name, subdomain_name, droplet_ip, headers):
    """
    Ensures an A record exists for the given domain name, subdomain name, and IP address.

    Args:
        domain_name (str): The domain name to query.
        subdomain_name (str): The subdomain name to query.
        droplet_ip (str): The IP address of the droplet to point to.
        headers (dict): The request headers.

    Returns:
        None
    """
    # Get the existing A record
    response = requests.get(f"{BASE_URL}/domains/{domain_name}/records", headers=headers)
    response.raise_for_status()
    records = response.json()["domain_records"]

    # Check if the A record already exists and check if it already points to the droplet_ip
    a_record_id = None
    for record in records:
        if record["type"] == "A" and record["name"] == subdomain_name:
            if record["data"] == droplet_ip:
                print(f"A record {subdomain_name}.{domain_name} already points to IP {droplet_ip}.")
                return
            a_record_id = record["id"]
            break

    # Delete the existing A record if it exists, and it doesn't point to the droplet_ip
    if a_record_id:
        print(f"A record {subdomain_name}.{domain_name} exists. Deleting...")
        requests.delete(f"{BASE_URL}/domains/{domain_name}/records/{a_record_id}", headers=headers)

    # Create a new A record
    payload = {
        "type": "A",
        "name": subdomain_name,
        "data": droplet_ip,
        "ttl": 3600
    }
    print(f"Creating new A record {subdomain_name}.{domain_name} pointing to {droplet_ip}...")
    requests.post(f"{BASE_URL}/domains/{domain_name}/records", json=payload, headers=headers)
    print(f"A record {subdomain_name}.{domain_name} created successfully.")

def parse_snapshot_name(snapshot_name):
    """
    Parse a snapshot name and return its constituent parts.

    Args:
        snapshot_name (str): The snapshot name to parse, in the format
            "tag-osname-droplet_size-region".

    Returns:
        tuple: A tuple containing the snapshot's tag, OS name, droplet size, and region.
    """
    parts = snapshot_name.split('-')
    tag = parts[0]
    osname = parts[1]
    droplet_size = f"{parts[2]}-{parts[3]}-{parts[4]}-{parts[5]}"
    region = parts[6]
    return tag, osname, droplet_size, region

def provision_droplet_and_configure_dns(body, headers):
    """
    Provisions a DigitalOcean droplet and configures DNS.

    Args:
        body (dict): The input data containing droplet and DNS details.
        headers (dict): The request headers.

    Returns:
        str: The IP address of the newly provisioned droplet.
    """
    droplet_name = body["droplet_name"]
    snapshot_name = body["snapshot_name"]
    tag_name = body["tag_name"]
    ssh_key_name = body["ssh_key_name"]
    domain_name = body["domain_name"]
    subdomain_name = body["subdomain_name"]
    tag, os_name, droplet_size, region = parse_snapshot_name(snapshot_name)
    droplet_full_name = f"{droplet_name}-{os_name}-{droplet_size}-{region}"
    # Check if the droplet already exists
    is_droplet_exists, droplet_id = droplet_exists(droplet_full_name, headers)
    if is_droplet_exists:
        print(f"Droplet {droplet_full_name} already exists with ID: {droplet_id}. .")
        droplet_ip = get_droplet_ip(droplet_id, headers)
        ensure_a_record(domain_name, subdomain_name, droplet_ip, headers)
        return droplet_ip


    # Create a new droplet
    print(f"Droplet {droplet_full_name} does not exist. Proceeding with creation.")
    snapshot_id = get_snapshot_id(snapshot_name, headers)
    ssh_key_id = get_ssh_key_id(ssh_key_name, headers)
    print(f"Creating droplet {droplet_full_name} with tag {tag}, OS {os_name}, size {droplet_size}, and region {region}...")
    droplet_id = create_droplet(droplet_full_name, snapshot_id, ssh_key_id, tag_name, region, droplet_size, headers=headers)
    print(f"Droplet {droplet_full_name} created with ID: {droplet_id}")

    # Get the droplet's IP address
    print("Waiting for droplet IP address assignment...")
    droplet_ip = get_droplet_ip(droplet_id, headers)

    # Configure DNS
    ensure_a_record(domain_name, subdomain_name, droplet_ip, headers)

    return droplet_ip

def droplets_by_tag(tag, headers):
    """
    Helper function to get all droplets by a specific tag.
    Returns a list of droplet IDs.
    """
    response = requests.get(f"{BASE_URL}/droplets?tag_name={tag}", headers=headers)
    response.raise_for_status()
    droplets = response.json()["droplets"]

    return [(droplet["id"], droplet["name"]) for droplet in droplets]

def delete_droplet(droplet_name_or_tag, headers, delete_by_tag=False):
    """
    Deletes an existing DigitalOcean droplet by its name or multiple droplets by tag.

    Args:
        droplet_name_or_tag (str): The name of the droplet or tag of droplets to be destroyed.
        delete_by_tag (bool): Set to True to delete droplets by tag. Default is False (delete by name).
        headers (dict): The request headers.
    Returns:
        None
    """
    if delete_by_tag:
        droplets_details = droplets_by_tag(droplet_name_or_tag, headers)
        if droplets_details:
            print(f"Destroying droplets with tag '{droplet_name_or_tag}'...")
            for droplets_detail in droplets_details:
                response = requests.delete(f"{BASE_URL}/droplets/{droplets_detail[0]}", headers=headers)
                response.raise_for_status()
                print(f"Droplet with ID {droplets_detail[0]} and name {droplets_detail[1]} destroyed successfully.")
        else:
            print(f"No droplets found with tag '{droplet_name_or_tag}'.")
    else:
        droplet_exists_flag, droplet_id = droplet_exists(droplet_name_or_tag)
        if droplet_exists_flag:
            print(f"Destroying droplet {droplet_name_or_tag} with ID: {droplet_id}...")
            response = requests.delete(f"{BASE_URL}/droplets/{droplet_id}", headers=headers)
            response.raise_for_status()
            print(f"Droplet {droplet_name_or_tag} destroyed successfully.")
        else:
            print(f"Droplet {droplet_name_or_tag} does not exist.")

def main(event, context):
    """
    The main function to create or delete a droplet and configure DNS.

    Args:
        args (dict): The input arguments provided to the function.

    Returns:
        dict: The HTTP response.
    """
    try:
        # Get the token from args or environment
        token = get_digitalocean_token(event)
        headers = {
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json"
        }

        # Parse input body
        body = event.get("body", {})
        action_type = event.get("action_type", "").strip().lower()

        if not action_type:
            return handle_response(400, {"error": "actionType is required"})

        # Handle droplet creation
        if action_type == "create":
            required_fields = [
                "snapshot_name", "droplet_name", "tag_name",
                "ssh_key_name", "domain_name", "subdomain_name"
            ]
            missing_fields = [field for field in required_fields if not body.get(field)]
            if missing_fields:
                return handle_response(400, {"error": f"Missing fields: {', '.join(missing_fields)}"})

            # Provision droplet and configure DNS
            droplet_ip = provision_droplet_and_configure_dns(body, headers)
            return handle_response(200, {"message": "Droplet created", "droplet_ip": droplet_ip})

        # Handle droplet deletion (if you plan to support this)
        elif action_type == "delete":
            if not body.get("droplet_name") and body.get("tag_name"):
                return handle_response(400, {"error": "either droplet_name or tag_name is required for deletion"})
            delete_droplet(body.get("tag_name") or body.get("droplet_name"), headers, body.get("tag_name") is not None)
            return handle_response(200, {"message": "Droplet deleted"})

        else:
            return handle_response(400, {"error": "Invalid actionType. Allowed: create, delete"})

    except json.JSONDecodeError as ex:
        return handle_response(400, {"error": "Invalid JSON in request body", "details": str(ex)})

    except requests.exceptions.RequestException as e:
        return handle_response(500, {"error": f"Request to DigitalOcean API failed: {str(e)}"})

    except Exception as e:
        return handle_response(500, {"error": f"An internal error occurred: {str(e)}"})


# if __name__ == "__main__":
#     main({
#         "action_type": "delete",
#         "body": {"snapshot_name": "oracle-ubuntu-s-2vcpu-4gb-amd-blr1", "droplet_name": "oracle-1",
#                  "tag_name": "oracle", "ssh_key_name": "Shravankumars-MacBook-Air.local", "domain_name": "shra012.com",
#                  "subdomain_name": "oracle1"}})
