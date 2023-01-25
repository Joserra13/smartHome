# Raspberry Pi 

## Installation

The Raspberry needs to have several aditional tools which are going to be installed by the command line.

### Update the system

      sudo apt-get update

      sudo apt-get upgrade

### Install Python

      sudo apt-get install python3.6

### Install Apache

      sudo apt install apache2
    
Check it is working

      sudo systemctl status apache2

### Install PHP

      sudo apt install php libapache2-mod-php php-mysql

Restart apache

      sudo systemctl restart apache2

Check it is working by opening this file

      sudo nano /var/www/html/info.php

Write this text

      <?php phpinfo(); ?>

Save and open this URL in the browser

      http://localhost/info.php

If everything went fine, you should be able to see the info of the PHP version installed.

## Install Composer

## Install Node-RED

      bash <(curl -sL https://raw.githubusercontent.com/node-red/linux-installers/master/deb/update-nodejs-and-nodered)

Once it is installed, load the [Node-RED flows](./noderedflows) into Node-RED following the steps explained in this [post](https://nodered.org/docs/user-guide/editor/workspace/import-export)