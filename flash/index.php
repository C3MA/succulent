<?PHP

$data="fb.gexf";
if (isset($_GET['id'])) {
	$id=$_GET['id'];
	if (is_numeric($id)) {
		if (!is_dir("/var/www/succulent/$id")) {
			$result="ID unknown!";
		}	
		else {
			if (file_exists("/var/www/succulent/$id/data.gexf")) {
				$result="Data for ID $id";
				$data="$id/data.gexf";
			}
			else {
				$result="Waiting for data ...";
			}
		}
	}
	else {
		$result="ID invalid!";
	}
}

if (isset($_POST['fbid'])) {
	$fbid=$_POST['fbid'];
	if (is_numeric($fbid)) {
		$newid=mt_rand();
	}

}


echo "
<html><title>Welcome to succulent</title>
<head>
<style type=\"text/css\">

body {
  color: white; background-color: black;
  font-size: 100%;
  font-family: Helvetica,Arial,sans-serif;
  margin: 0; padding: 1em;
}

</style>
<body>
<h1>Succulent Facebook stalking tool! <font color=#FF7979>$result</font></h1>
<table>
<tr>
<td>
<object width=\"1024\" height=\"700\" id=\"GexfExplorer\">
	<param name=\"movie\" value=\"GexfExplorer1.0.swf?path=$data&clickableAttribute=url&curvedEdges=false&labelsColor=0xFFFFFF&font=Verdana&showAttributes=true\" />
	<param name=\"allowFullScreen\" value=\"true\" />
	<param name=\"bgcolor\" value=\"#000000\" />
	<embed src=\"GexfExplorer1.0.swf?path=$data&clickableAttribute=url&curvedEdges=false&labelsColor=0xFFFFFF&font=Verdana&showAttributes=true\" allowFullScreen=\"true\" bgcolor=\"#000000\" width=\"1024\" height=\"700\">
	</embed>
</object>
</td>
<td valign=\"top\">
<table>
<tr>
<td>";




echo"</td>
</tr>
</table>
</td>
</tr>
<table>
";
?>
