package server;
import java.awt.*;
class InfoWin extends Frame{
TextArea infoBox;
public InfoWin(){
	infoBox=new TextArea(5,36);
	add(infoBox);
	validate();
	}
}
