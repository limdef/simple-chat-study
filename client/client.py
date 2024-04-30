import socket
import threading
import sys
import os, signal

exit_event = threading.Event()

def validate_port(port):
    try:
        p = int(port)
        if 1 <= p <= 65535:
            return True
    except:
        return False

if len(sys.argv) != 2 or validate_port(sys.argv[1]) == False:
    print("invalid argument")
    sys.exit()

port = int(sys.argv[1])
print(f"port number {port}")

try:
    client = socket.socket(socket.AF_INET, socket.SOCK_STREAM) # AF_INET : IPv4 , SOCK_STREAM : TCP
    client.connect(('127.0.0.1', port))
except Exception as e:
    print(f"connection failed : {e}")
    sys.exit()

def close():
    exit_event.set()
    client.close()

def handler(signum, frame):
    close()

signal.signal(signal.SIGINT, handler)
signal.signal(signal.SIGTERM, handler)

def client_receive():
    while not exit_event.is_set():
        try:
            # recv는 블로킹 함수, 1024는 버퍼 사이즈
            # 도착한 데이터가 버퍼보다 작으면 반환 후 종료. 크면 버퍼 사이즈만큼 반환 후 소켓 버퍼에 데이터가 남아있음.
            msg = client.recv(1024).decode('utf-8')
            if msg:
                print(msg)
            else:
                print("Server has closed the connection")
                break
        except OSError as e:
            if e.errno == 9:
                break
            print(f"RECEIVE ERROR : {e}")
            break
        except Exception as e:
            print(f"RECEIVE ERROR : {e}")
            break
    close()

def client_send():
    print("Please enter a message (or type 'quit' to quit) \n")
    msg = ''
    while not exit_event.is_set():
        try:
            user_input = input('')
            user_input = user_input.strip()

            if user_input == '':
                continue
            
            if user_input.lower() == 'quit':
                print('terminating...')
                break

            # '\'로 끝나면 기존 메시지에 추가
            if user_input.endswith('\\'):
                msg += user_input.rstrip('\\')
                continue
            
            msg += user_input

            msg_bytes = msg.encode('utf-8')
            msg_len = len(msg_bytes)

            if msg_len > 1024:
                raise OverflowError

            client.send(msg_len.to_bytes(4, byteorder='big')) # len의 정수값을 4바이트의 빅엔디안 바이트 열
            client.send(msg_bytes)

            msg = ''
        
        except OverflowError:
            msg = ''
            print("Message length is too long. Please retry to enter a message.")

        except Exception as e:
            print(f"SEND ERROR : {e}")
            break
    close()
    

receive_thread = threading.Thread(target=client_receive)
receive_thread.start()

send_thread = threading.Thread(target=client_send)
send_thread.start()

# 두 스레드 중 하나만 종료되어도 메인 프로그램 종료
exit_event.wait()
print("Main Program Terminated...")
os._exit(0) # 즉시 종료