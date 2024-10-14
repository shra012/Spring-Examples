import requests
import time
import os
import json

# Set up base URL and API token (replace with your actual API token)
API_TOKEN = os.environ.get("DIGITALOCEAN_TOKEN")
BASE_URL = "https://api.digitalocean.com/v2"
HEADERS = {
    "Authorization": f"Bearer {API_TOKEN}",
    "Content-Type": "application/json"
}

# Function to get a list of droplets and check if a droplet exists by name
def droplet_exists(droplet_name):
    response = requests.get(f"{BASE_URL}/droplets", headers=HEADERS)
    response.raise_for_status()
    droplets = response.json()["droplets"]

    for droplet in droplets:
        if droplet["name"] == droplet_name:
            return True
    return False

# Function to get the snapshot by name
def get_snapshot_id(snapshot_name):
    response = requests.get(f"{BASE_URL}/snapshots", headers=HEADERS)
    response.raise_for_status()
    snapshots = response.json()["snapshots"]

    for snapshot in snapshots:
        if snapshot["name"] == snapshot_name:
            return snapshot["id"]
    raise Exception(f"Snapshot {snapshot_name} not found.")

# Function to get SSH key ID by name
def get_ssh_key_id(ssh_key_name):
    response = requests.get(f"{BASE_URL}/account/keys", headers=HEADERS)
    response.raise_for_status()
    keys = response.json()["ssh_keys"]

    for key in keys:
        if key["name"] == ssh_key_name:
            return key["id"]
    raise Exception(f"SSH key {ssh_key_name} not found.")

# Function to create a new droplet
def create_droplet(droplet_name, snapshot_id, ssh_key_id, tag_name, region="nyc1", size="s-1vcpu-1gb"):
    payload = {
        "name": droplet_name,
        "region": region,
        "size": size,
        "image": snapshot_id,
        "ssh_keys": [ssh_key_id],
        "ipv6": True,  # Enable IPv6
        "tags": [tag_name]
    }

    response = requests.post(f"{BASE_URL}/droplets", json=payload, headers=HEADERS)
    response.raise_for_status()
    droplet_id = response.json()["droplet"]["id"]
    return droplet_id

# Function to poll the droplet status and wait for the IP address to be assigned
def get_droplet_ip(droplet_id):
    while True:
        response = requests.get(f"{BASE_URL}/droplets/{droplet_id}", headers=HEADERS)
        response.raise_for_status()
        droplet = response.json()["droplet"]
        networks = droplet["networks"]

        # Check if IPv4 and IPv6 addresses are assigned
        v4_ip = next((net["ip_address"] for net in networks["v4"] if net["type"] == "public"), None)
        v6_ip = next((net["ip_address"] for net in networks["v6"] if net["type"] == "public"), None)

        if v4_ip:
            print(f"IPv4: {v4_ip}")
        if v6_ip:
            print(f"IPv6: {v6_ip}")

        if v4_ip:  # We require at least IPv4, but you can wait for IPv6 as well if needed
            return v4_ip

        print("Waiting for the droplet to be fully provisioned...")
        time.sleep(10)  # Delay for 10 seconds before checking again

# Function to get the A record ID (if exists) for a subdomain in a domain
def get_a_record_id(domain_name, subdomain_name):
    response = requests.get(f"{BASE_URL}/domains/{domain_name}/records", headers=HEADERS)
    response.raise_for_status()
    records = response.json()["domain_records"]

    for record in records:
        if record["type"] == "A" and record["name"] == subdomain_name:
            return record["id"]
    return None

# Function to delete an existing A record
def delete_a_record(domain_name, record_id):
    response = requests.delete(f"{BASE_URL}/domains/{domain_name}/records/{record_id}", headers=HEADERS)
    response.raise_for_status()

# Function to create a new A record pointing to the droplet's IP address
def create_a_record(domain_name, subdomain_name, ip_address):
    payload = {
        "type": "A",
        "name": subdomain_name,
        "data": ip_address,
        "ttl": 3600
    }
    response = requests.post(f"{BASE_URL}/domains/{domain_name}/records", json=payload, headers=HEADERS)
    response.raise_for_status()

# Main function to orchestrate the droplet creation and domain mapping
def provision_droplet_and_configure_dns(body):
    droplet_name = body["droplet_name"]
    snapshot_name = body["snapshot_name"]
    tag_name = body["tag_name"]
    ssh_key_name = body["ssh_key_name"]
    domain_name = body["domain_name"]
    subdomain_name = body["subdomain_name"]

    # Step 1: Check if droplet already exists
    if droplet_exists(droplet_name):
        print(f"Droplet {droplet_name} already exists. Exiting.")
        return

    # Step 2: Get the snapshot ID and SSH key ID
    snapshot_id = get_snapshot_id(snapshot_name)
    ssh_key_id = get_ssh_key_id(ssh_key_name)

    # Step 3: Create the droplet
    print(f"Creating droplet {droplet_name}...")
    droplet_id = create_droplet(droplet_name, snapshot_id, ssh_key_id, tag_name)
    print(f"Droplet {droplet_name} created with ID: {droplet_id}")

    # Step 4: Wait for the droplet to be assigned an IP address
    print("Waiting for droplet IP address assignment...")
    droplet_ip = get_droplet_ip(droplet_id)

    # Step 5: Check if A record exists for the domain and subdomain
    a_record_id = get_a_record_id(domain_name, subdomain_name)
    if a_record_id:
        print(f"A record {subdomain_name}.{domain_name} exists. Deleting...")
        delete_a_record(domain_name, a_record_id)

    # Step 6: Create a new A record pointing to the new droplet's IP
    print(f"Creating new A record {subdomain_name}.{domain_name} pointing to {droplet_ip}...")
    create_a_record(domain_name, subdomain_name, droplet_ip)
    print(f"A record {subdomain_name}.{domain_name} created successfully.")


def main(event, context):
    """
    This function handles the creation of a droplet, validates input, and returns a response.
    It expects a JSON body with the following fields:
    {
        "snapshot_name": "required",
        "droplet_name": "required",
        "tag_name": "required",
        "ssh_key_name": "required",
        "domain_name": "required",
        "subdomain_name": "required"
    }
    """

    try:
        # Step 1: Ensure the request body is valid JSON
        body = json.loads(event.get("body", "{}"))

        # Step 2: Validate that the required fields are provided
        required_fields = [
            "snapshot_name",
            "droplet_name",
            "tag_name",
            "ssh_key_name",
            "domain_name",
            "subdomain_name"
        ]

        missing_fields = [field for field in required_fields if field not in body or not isinstance(body[field], str) or not body[field].strip()]

        if missing_fields:
            return {
                "statusCode": 400,
                "body": json.dumps({
                    "error": "Missing or invalid fields: " + ", ".join(missing_fields)
                })
            }
        provision_droplet_and_configure_dns(body)
        # For demonstration purposes, let's just return a success message with the input
        return {
            "statusCode": 200,
            "body": json.dumps({
                "message": "Request body is valid. Proceeding with droplet creation.",
                "data": body
            })
        }

    except json.JSONDecodeError:
        # Handle JSON parsing errors
        return {
            "statusCode": 400,
            "body": json.dumps({"error": "Invalid JSON in request body"})
        }

    except Exception as e:
        # Handle any other unforeseen errors
        return {
            "statusCode": 500,
            "body": json.dumps({"error": str(e)})
        }

if __name__ == "__main__":
    main({"body" : {
        "snapshot_name": "oracle-ubuntu-s-2vcpu-4gb-amd-blr1",
        "droplet_name": "oracle-1",
        "tag_name": "oracle",
        "ssh_key_name": "Shravankumars-MacBook-Air.local",
        "domain_name": "shra012.com",
        "subdomain_name": "oracle1"
    }}, None)