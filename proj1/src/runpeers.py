# java peer.Server 1.0 1 Hello 230.0.0.0:4446 230.0.0.0:4445 230.0.0.0:4444


import subprocess, pathlib, os, atexit, sys, time

if len(sys.argv) != 2:
    print("python3 runpeers.py <npeers>")
    exit()


npeers = int(sys.argv[1])
src = pathlib.Path(__file__).parent
out = src.parent / 'out'
os.chdir(out)

mc =  "230.0.0.0:4446"
mdb = "230.0.0.0:4445"
mdr = "230.0.0.0:4444"

peers = {}

rmiregistry = subprocess.Popen("rmiregistry")

print(f"rmiregistry process spawned with PID {rmiregistry.pid}")

time.sleep(1)

for i in range(npeers):
    args = f"java peer.Server 1.0 {i} peer{i} {mc} {mdb} {mdr}"
    print(f"Executing command '{args}'")
    peers[i] = subprocess.Popen(args, shell=True)

def kill_zombies():
    rmiregistry.kill()
    for i in peers:
        peers[i].kill()

rmiregistry.wait()

atexit.register(kill_zombies)