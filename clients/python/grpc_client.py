import grpc
import url_pb2
import url_pb2_grpc

def list_user_urls(username):
    with grpc.insecure_channel('localhost:50051') as channel:
        stub = url_pb2_grpc.UrlServiceStub(channel)
        request = url_pb2.ListUserUrlsRequest(username=username)
        try:
            response = stub.ListUserUrls(request)
            print(f"Listado de URLs para '{username}':")
            for record in response.urls:
                print("------------------------------------------------")
                print("URL Original: ", record.originalUrl)
                print("URL Acortada: ", record.shortUrl)
                print("Fecha de Creación: ", record.creationDate)
                print("Accesos: ", record.accessCount)
                print("Vista Previa (Base64): ", record.previewImageBase64)
            print("------------------------------------------------")
        except grpc.RpcError as e:
            print("Error al obtener listado:", e.details())

def create_url_record(username, url):
    with grpc.insecure_channel('localhost:50051') as channel:
        stub = url_pb2_grpc.UrlServiceStub(channel)
        request = url_pb2.CreateUrlRecordRequest(username=username, url=url)
        try:
            response = stub.CreateUrlRecord(request)
            record = response.record
            print("Registro de URL Creado:")
            print("------------------------------------------------")
            print("URL Original: ", record.originalUrl)
            print("URL Acortada: ", record.shortUrl)
            print("Fecha de Creación: ", record.creationDate)
            print("Accesos: ", record.accessCount)
            print("Vista Previa (Base64): ", record.previewImageBase64)
            print("------------------------------------------------")
        except grpc.RpcError as e:
            print("Error al crear registro:", e.details())

if __name__ == "__main__":
    # Ejemplo de uso
    usuario = "usuario1"
    url_original = "http://ejemplo.com"

    print("Creando registro de URL...")
    create_url_record(usuario, url_original)

    print("\nListando registros de URL para el usuario...")
    list_user_urls(usuario)
