package com.example.dm2.a2048vasco;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class GameView extends LinearLayout {

	public GameView(Context context) {
		super(context);

		initGameView();
	}

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);

		initGameView();
	}

	private void initGameView(){
		setOrientation(LinearLayout.VERTICAL);
		setBackgroundColor(0xffbbada0);

		//Capturamos raton
		setOnTouchListener(new View.OnTouchListener() {

			private float startX,startY,offsetX,offsetY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {

					//Capturamos el punto de inicio
					case MotionEvent.ACTION_DOWN:
						startX = event.getX();
						startY = event.getY();
						break;
					//Capturamos la diferencia de posición cuando termina
					case MotionEvent.ACTION_UP:
						offsetX = event.getX()-startX;
						offsetY = event.getY()-startY;

						//Escogemos el mayor cambio y realizamos una acción en esa dirección
						//  Establecemos un margen de error (pulsa y no arrastra) de 5px
						if (Math.abs(offsetX)> Math.abs(offsetY)) {
							if (offsetX<-5) {
								swipeLeft();
							}else if (offsetX>5) {
								swipeRight();
							}
						}else{
							if (offsetY<-5) {
								swipeUp();
							}else if (offsetY>5) {
								swipeDown();
							}
						}

						break;
					}
				return true;
			}
		});
	}

	//Capturamos el cambio de tamaño para darle a nuestras celdas el tamaño correcto
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		Config.CARD_WIDTH = (Math.min(w, h)-10)/Config.LINES;

		addCards(Config.CARD_WIDTH,Config.CARD_WIDTH);

		startGame();
	}

	//Pinta las celdas en el layout
	private void addCards(int cardWidth,int cardHeight){

		Card c;

		LinearLayout line;
		LinearLayout.LayoutParams lineLp;

		//Añade tantas líneas como se haya configurado
		for (int y = 0; y < Config.LINES; y++) {
			line = new LinearLayout(getContext());
			lineLp = new LinearLayout.LayoutParams(-1, cardHeight);
			addView(line, lineLp);

			//Añade tantas columnas como líneas
			for (int x = 0; x < Config.LINES; x++) {
				c = new Card(getContext());
				line.addView(c, cardWidth, cardHeight);

				cardsMap[x][y] = c;
			}
		}
	}

	//Inicia el juego
	public void startGame(){

		MainActivity aty = MainActivity.getMainActivity();

		//Limpia la puntuación
		aty.clearScore();

		//Muestra la puntuación máxima
		aty.showBestScore(aty.getBestScore());

		//Establece el valor de todas las celdas a 0
		for (int y = 0; y < Config.LINES; y++) {
			for (int x = 0; x < Config.LINES; x++) {
				cardsMap[x][y].setNum(0);
			}
		}

		//Añade dos celdas con valor
		addRandomNum();
		addRandomNum();
	}

	private void addRandomNum(){

		//Limpia el listado temporal
		emptyPoints.clear();

		//Recoge todas las celdas "vacías"
		for (int y = 0; y < Config.LINES; y++) {
			for (int x = 0; x < Config.LINES; x++) {
				if (cardsMap[x][y].getNum()<=0) {
					emptyPoints.add(new Point(x, y));
				}
			}
		}

		//Comprueba si hay celdas vacías, elige una al azar, le asigna valor y inicia la animación de creación
		if (emptyPoints.size()>0) {

			Point p = emptyPoints.remove((int)(Math.random()*emptyPoints.size()));
			cardsMap[p.x][p.y].setNum(Math.random()>0.1?2:4);

			MainActivity.getMainActivity().getAnimLayer().createScaleTo1(cardsMap[p.x][p.y]);
		}
	}


	//Si arrastramos a la izquierda
	private void swipeLeft(){

		//Si se juntarán o no
		boolean merge = false;

		//Comprueba todas las celdas y la que tenga inmediatamente a la izquierda para ver si coinciden y se juntan o no
		for (int y = 0; y < Config.LINES; y++) {
			for (int x = 0; x < Config.LINES; x++) {

				for (int x1 = x+1; x1 < Config.LINES; x1++) {
					if (cardsMap[x1][y].getNum()>0) {

						if (cardsMap[x][y].getNum()<=0) {

							MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardsMap[x1][y],cardsMap[x][y], x1, x, y, y);

							cardsMap[x][y].setNum(cardsMap[x1][y].getNum());
							cardsMap[x1][y].setNum(0);

							x--;
							merge = true;

						}else if (cardsMap[x][y].equals(cardsMap[x1][y])) {
							MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardsMap[x1][y], cardsMap[x][y],x1, x, y, y);
							cardsMap[x][y].setNum(cardsMap[x][y].getNum()*2);
							cardsMap[x1][y].setNum(0);

							MainActivity.getMainActivity().addScore(cardsMap[x][y].getNum());
							merge = true;
						}

						break;
					}
				}
			}
		}

		//Si coinciden lanzamos la función para rellenar una celda vacía y comprobamos que se pueda continuar el juego
		if (merge) {
			addRandomNum();
			checkComplete();
		}
	}

	//Si arrastramos a la derecha
	private void swipeRight(){

		//Si se juntarán o no
		boolean merge = false;

		//Comprueba todas las celdas y la que tenga inmediatamente a la derecha para ver si coinciden y se juntan o no
		for (int y = 0; y < Config.LINES; y++) {
			for (int x = Config.LINES-1; x >=0; x--) {

				for (int x1 = x-1; x1 >=0; x1--) {
					if (cardsMap[x1][y].getNum()>0) {

						if (cardsMap[x][y].getNum()<=0) {
							MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardsMap[x1][y], cardsMap[x][y],x1, x, y, y);
							cardsMap[x][y].setNum(cardsMap[x1][y].getNum());
							cardsMap[x1][y].setNum(0);

							x++;
							merge = true;
						}else if (cardsMap[x][y].equals(cardsMap[x1][y])) {
							MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardsMap[x1][y], cardsMap[x][y],x1, x, y, y);
							cardsMap[x][y].setNum(cardsMap[x][y].getNum()*2);
							cardsMap[x1][y].setNum(0);
							MainActivity.getMainActivity().addScore(cardsMap[x][y].getNum());
							merge = true;
						}

						break;
					}
				}
			}
		}

		//Si coinciden lanzamos la función para rellenar una celda vacía y comprobamos que se pueda continuar el juego
		if (merge) {
			addRandomNum();
			checkComplete();
		}
	}

	//Si arrastramos arriba
	private void swipeUp(){

		//Si se juntarán o no
		boolean merge = false;

		//Comprueba todas las celdas y la que tenga inmediatamente a arriba para ver si coinciden y se juntan o no
		for (int x = 0; x < Config.LINES; x++) {
			for (int y = 0; y < Config.LINES; y++) {

				for (int y1 = y+1; y1 < Config.LINES; y1++) {
					if (cardsMap[x][y1].getNum()>0) {

						if (cardsMap[x][y].getNum()<=0) {
							MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardsMap[x][y1],cardsMap[x][y], x, x, y1, y);
							cardsMap[x][y].setNum(cardsMap[x][y1].getNum());
							cardsMap[x][y1].setNum(0);

							y--;

							merge = true;
						}else if (cardsMap[x][y].equals(cardsMap[x][y1])) {
							MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardsMap[x][y1],cardsMap[x][y], x, x, y1, y);
							cardsMap[x][y].setNum(cardsMap[x][y].getNum()*2);
							cardsMap[x][y1].setNum(0);
							MainActivity.getMainActivity().addScore(cardsMap[x][y].getNum());
							merge = true;
						}

						break;

					}
				}
			}
		}

		//Si coinciden lanzamos la función para rellenar una celda vacía y comprobamos que se pueda continuar el juego
		if (merge) {
			addRandomNum();
			checkComplete();
		}
	}

	//Si arrastramos abajo
	private void swipeDown(){

		//Si se juntarán o no
		boolean merge = false;

		//Comprueba todas las celdas y la que tenga inmediatamente a abajo para ver si coinciden y se juntan o no
		for (int x = 0; x < Config.LINES; x++) {
			for (int y = Config.LINES-1; y >=0; y--) {

				for (int y1 = y-1; y1 >=0; y1--) {
					if (cardsMap[x][y1].getNum()>0) {

						if (cardsMap[x][y].getNum()<=0) {
							MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardsMap[x][y1],cardsMap[x][y], x, x, y1, y);
							cardsMap[x][y].setNum(cardsMap[x][y1].getNum());
							cardsMap[x][y1].setNum(0);

							y++;
							merge = true;
						}else if (cardsMap[x][y].equals(cardsMap[x][y1])) {
							MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardsMap[x][y1],cardsMap[x][y], x, x, y1, y);
							cardsMap[x][y].setNum(cardsMap[x][y].getNum()*2);
							cardsMap[x][y1].setNum(0);
							MainActivity.getMainActivity().addScore(cardsMap[x][y].getNum());
							merge = true;
						}

						break;
					}
				}
			}
		}

		//Si coinciden lanzamos la función para rellenar una celda vacía y comprobamos que se pueda continuar el juego
		if (merge) {
			addRandomNum();
			checkComplete();
		}
	}

	//Comprueba si el jugador puede realizar más movimientos, es decir, si el juego ha acabado o no
	private void checkComplete(){

		boolean complete = true;

		//Comprueba si cualquier celda está vacía o si alguna celda vecina coincide
		ALL:
			for (int y = 0; y < Config.LINES; y++) {
				for (int x = 0; x < Config.LINES; x++) {
					if (cardsMap[x][y].getNum()==0||
							(x>0&&cardsMap[x][y].equals(cardsMap[x-1][y]))||
							(x<Config.LINES-1&&cardsMap[x][y].equals(cardsMap[x+1][y]))||
							(y>0&&cardsMap[x][y].equals(cardsMap[x][y-1]))||
							(y<Config.LINES-1&&cardsMap[x][y].equals(cardsMap[x][y+1]))) {

						complete = false;
						break ALL;
					}
				}
			}

		//Si ha terminado muestra un mensaje al usurio y permite reiniciar el juego
		if (complete) {
			new AlertDialog.Builder(getContext()).setTitle("BUEN INTENTO").setMessage("Ya no puedes realizar más movimientos").setPositiveButton("Reiniciar", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					startGame();
				}
			}).show();
		}

	}

	private Card[][] cardsMap = new Card[Config.LINES][Config.LINES];
	private List<Point> emptyPoints = new ArrayList<Point>();
}
