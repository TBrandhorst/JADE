# Setzen von GIT_SSL_NO_VERIFY
export GIT_SSL_NO_VERIFY="true"

# Erstellen des Arbeitsverzeichnisses und Navigieren in dieses
mkdir -p jadeSVN
cd jadeSVN

# Konfiguration von Git und Ausführen der Git-SVN-Initialisierung
git config --global http.sslVerify false
expect -c "
    set timeout -1
    spawn git svn init https://jade.tilab.com/svn/jade/trunk/
    expect {
        \"(R)eject, accept (t)emporarily or accept (p)ermanently?\" {
            send \"p\r\"
            exp_continue
        }
        eof
    }
"

# Weitere Git-Konfigurationen
git config --global svn.sslVerify false
git config svn.authorsfile "$1"

# Berechnung der Revisionsnummer und Synchronisation mit SVN
RevNumber=$(grep -Eo '[0-9\.]+' "$2")
sub=20
RevNumber=$(($RevNumber-$sub))
git svn fetch -r$RevNumber:HEAD

# Hinzufügen eines entfernten Repositories und Rebase mit SVN
git remote add JADE-SVN-TILAB https://github.com/TBrandhorst/JADE.git
git svn rebase