<?php
namespace MyApp;
use Ratchet\MessageComponentInterface;
use Ratchet\ConnectionInterface;

function readData(){
	//$readData = "1 0 435 1 26.30 60.00 1";
	//Read the values
	$command = "python readData.py";
	$output = shell_exec($command);
	//$output = $readData;

	//Parse the data

	$i = 0;

	do{
		$i++;
		$space = strpos($output, ' ');

		if($space === false){
			$isNight = substr($output, 0);
			$i = 0;			
		}else{
			if($i == 1){
				$stateAlarm = substr($output, 0, $space);			
			}else if($i == 2){
				$nDetections = substr($output, 0, $space);			
			}else if($i == 3){
				$gasValue = substr($output, 0, $space);			
			}else if($i == 4){
				$windowIsOpen = substr($output, 0, $space);			
			}else if($i == 5){
				$temp = substr($output, 0, $space);			
			}else if($i == 6){
				$hum = substr($output, 0, $space);			
			}
		}	
		$output = substr($output, $space+1);
		
	}while($space !== false);

	//Create a JSON with the data

	$arrayData = array("stateAlarm" => $stateAlarm, "nDetections" => $nDetections, "gasValue" => $gasValue, "windowIsOpen" => $windowIsOpen, "temp" => $temp, "hum"=> $hum);

	return	$arrayData;

	//Send the data to the DDBB
}

class Chat implements MessageComponentInterface {

    private $clients;
    
    public function __construnct() {
        $this->clients = array();
    }

    public function onOpen(ConnectionInterface $conn) {
        $this->clients[] = $conn;

        echo "New Connection";

		$arrayDataParsed = readData();

		//Send the data to the App
		foreach ($this->clients as $client) {
			$client->send(json_encode($arrayDataParsed));
			echo json_encode($arrayDataParsed);
		}

    }

    public function onMessage(ConnectionInterface $from, $msg) {

        $numRecv = count($this->clients) - 1;
        //echo sprintf('Connection %d sending message "%s" to %d other connection%s' . "\n"
        //     , $from->resourceId, $msg, $numRecv, $numRecv == 1 ? '' : 's');

		if($msg[0] == 'A'){
			$value = substr($msg, 2);
			echo $value;		
			if($value == 'true'){
				//Turn on the Alarm
				$command = "python alarmOn.py";
				$output = shell_exec($command);
				echo "\n\nTurn On\n\n";
			} else if($value == 'false'){
				//Turn off the alarm
				$command = "python alarmOff.py";
				$output = shell_exec($command);
				echo "\n\nTurn Off\n\n";
			}			
		} else if($msg[0] == 'W'){
			
			$value = substr($msg, 2);
			//echo $value;	
			if($value == 'true'){
				//Open the window
				$command = "python openW.py";
				$output = shell_exec($command);
				echo "\n\nOpen Window\n\n";
			} else if($value == 'false'){
				//Close the window
				$command = "python closeW.py";
				$output = shell_exec($command);
				echo "\n\nClose Window\n\n";
			}	
		} else if($msg[0] == 'R'){
			
			$arrayDataParsed = readData();

			//Send the data to the App
			foreach ($this->clients as $client) {
				$client->send(json_encode($arrayDataParsed));
				echo json_encode($arrayDataParsed);
				echo "\n";
			}
		}
    }

    public function onClose(ConnectionInterface $conn) {

        echo "Connection closed";

    }

    public function onError(ConnectionInterface $conn, \Exception $e) {
        echo $e->getMessage();
    }
}
