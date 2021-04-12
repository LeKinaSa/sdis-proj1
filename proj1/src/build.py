import subprocess, pathlib, os, shutil

src = pathlib.Path(__file__).parent
out = src.parent / 'out'

os.chdir(src)

try:
    shutil.rmtree(out / 'peer') # Remove what was there before
except FileNotFoundError:
    pass

command = "javac -cp \".:./gson-2.8.6.jar\" peer/*.java peer/clientCommands/*.java peer/messages/*.java peer/state/*.java -d ../out"

print(command)
subprocess.check_call(command, shell=True)