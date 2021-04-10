import subprocess, pathlib, os, shutil

src = pathlib.Path(__file__).parent
out = src.parent / 'out'

os.chdir(src)

try:
    shutil.rmtree(out / 'peer') # Remove what was there before
except FileNotFoundError:
    pass

directories = ["peer", "peer/clientCommands"]
print(" ".join(["javac"] + list(map(lambda x : x + "/*.java", directories)) + ["-d", "../out"]))
subprocess.check_call(" ".join(["javac"] + list(map(lambda x : x + "/*.java", directories)) + ["-d", "../out"]), shell=True)