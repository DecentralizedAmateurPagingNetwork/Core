Change to file to your needs.

It is supposed to have a user ``dapnet`` who runs the core and the maven compiled jar file in /opt/dapnet/Core/target.
All the config and data sub-dirs should be in /opt/dapnet/Core/local

Copy the service file to /etc/systemd/system/dapnetcore.service and run as root

``systemctl daemon-reload``

Then start as root with

``systemctl start dapnetcore``

Enable start at boot with

``systemctl enable dapnetcore``

