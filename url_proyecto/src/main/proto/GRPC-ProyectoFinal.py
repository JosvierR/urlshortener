#!/usr/bin/env python3
import argparse
import grpc
import url_pb2
import url_pb2_grpc

def create_url(stub, username, original_url, token=None):
    request = url_pb2.CreateUrlRecordRequest(username=username, originalUrl=original_url)
    metadata = [("authorization", f"Bearer {token}")] if token else []
    response = stub.CreateUrlRecord(request, metadata=metadata)
    print("✅ URL acortada:")
    print("Original:", original_url)
    print("Short URL:", response.urlRecord.shortUrl)
    print("Código:", response.urlRecord.shortUrl.split("/")[-1])
    print("Accesos:", response.urlRecord.accessCount)

def list_urls(stub, username, token=None):
    request = url_pb2.ListUserUrlsRequest(username=username)
    metadata = [("authorization", f"Bearer {token}")] if token else []
    response = stub.ListUserUrls(request, metadata=metadata)
    print(f"📄 URLs para {username}:")
    for url in response.urls:
        print(f"- {url.originalUrl} → {url.shortUrl} ({url.accessCount} accesos)")

def main():
    parser = argparse.ArgumentParser(description="Cliente gRPC para URL Shortener")
    parser.add_argument("--host", default="localhost", help="Host del servidor gRPC")
    parser.add_argument("--port", default="50051", help="Puerto del servidor gRPC")
    parser.add_argument("--token", help="JWT opcional para autenticación")

    subparsers = parser.add_subparsers(dest="command")

    # Subcomando: shorten
    parser_create = subparsers.add_parser("shorten", help="Acortar una URL")
    parser_create.add_argument("--username", required=True, help="Usuario que acorta")
    parser_create.add_argument("--url", required=True, help="URL original a acortar")

    # Subcomando: list
    parser_list = subparsers.add_parser("list", help="Listar URLs de un usuario")
    parser_list.add_argument("--username", required=True, help="Usuario a consultar")

    args = parser.parse_args()

    channel = grpc.insecure_channel(f"{args.host}:{args.port}")
    stub = url_pb2_grpc.UrlExtendedServiceStub(channel)

    if args.command == "shorten":
        create_url(stub, args.username, args.url, args.token)
    elif args.command == "list":
        list_urls(stub, args.username, args.token)
    else:
        parser.print_help()

if __name__ == "__main__":
    main()
