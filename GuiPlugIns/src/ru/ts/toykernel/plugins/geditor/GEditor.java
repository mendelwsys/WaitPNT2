package ru.ts.toykernel.plugins.geditor;

import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.plugins.IGuiModule;
import ru.ts.toykernel.plugins.IAnswerBean;
import ru.ts.toykernel.plugins.ICommandBean;
import ru.ts.toykernel.plugins.CommandBean;
import ru.ts.toykernel.consts.INameConverter;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.gui.IViewControl;
import ru.ts.toykernel.filters.IBaseFilter;
import ru.ts.toykernel.filters.IKeySelFilter;
import ru.ts.toykernel.filters.DefMBBFilter;
import ru.ts.toykernel.filters.DefSelFilterByKeys;
import ru.ts.toykernel.storages.IEditableStorage;
import ru.ts.toykernel.geom.IEditableGisObject;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.geom.IGisObject;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.converters.IProjConverter;
import ru.ts.toykernel.shared.DefGisOperations;
import ru.ts.factory.IFactory;
import ru.ts.factory.IParam;
import ru.ts.res.ImgResources;
import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.gisutils.algs.common.MRect;
import ru.ts.utils.data.Pair;
import su.mwlib.utils.Enc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Редактор геометрии объектов
 * ru.ts.toykernel.plugins.geditor.GEditor
 */
public class GEditor extends BaseInitAble implements IGuiModule
{

	public static final String MODULENAME = "GEDITOR";

	//-------------------------------------------------------------------------------------------//
	public static final int NON_EDIT = 0;
	//-------------------------------------------------------------------------------------------//
	public static final int ADD_POINT = 10;

	public static final int ADD_LINE = 20;
	public static final int IN_LINE = 21;
	//-------------------------------------------------------------------------------------------//
	public static final int CONV_LINE = 30;
	public static final int CONV_IN_LINE = 31;
	//-------------------------------------------------------------------------------------------//
	public static final int MOVE_P_OBJECT = 40;
	public static final int MOVE_IN_P_OBJECT = 41;

	public static final int MOVE_SEGMENT_OBJECT = 50;
	public static final int MOVE_IN_SEGMENT_OBJECT = 51;

	public static final int MOVE_OBJECT = 60;
	public static final int MOVE_IN_OBJECT = 61;
//-------------------------------------------------------------------------------------------//

	public static final int ADD_P_OBJECT = 80;  //с МОДИФИКАТОРОМ CTRL ДОБАВИТЬ ПОСЛЕ, БЕЗ НЕГО ДОБАВИТЬ ПЕРЕД (ESC ДОЛЖЕН ОТКАТИТЬ ИЗМЕНЕНИЯ ТЕКУЩЕГО ОБЪЕКТА)
	public static final int ADD_IN_P_OBJECT = 81;

	public static final int ADD_SEGMENT_OBJECT = 90; //Для политочек точек и для полилиний
	public static final int ADD_IN_SEGMENT_OBJECT = 91;
	public static final int ADD_IN_P_SEGMENT_OBJECT = 92;

	//-------------------------------------------------------------------------------------------//
	public static final int MERGE_OBJECT = 100;
	public static final int MERGE_IN_OBJECT = 101;

	public static final int SPLIT_OBJECT = 110;
	public static final int SPLIT_IN_OBJECT = 111;
	//-------------------------------------------------------------------------------------------//

	public static final int REMOVE_P_OBJECT = 120;
	public static final int REMOVE_IN_P_OBJECT = 121;

	public static final int REMOVE_SEGMENT_OBJECT = 130;
	public static final int REMOVE_IN_SEGMENT_OBJECT = 131;

	public static final int REMOVE_OBJECT = 140;
	public static final int REMOVE_IN_OBJECT = 141;
	//-------------------------------------------------------------------------------------------//
	//-------------------------------------------------------------------------------------------//
	public static final String SET_ADDPOINTOBJ_H = Enc.get("ADD_POINT");
	public static final String SET_ADDLINEOBJ_H = Enc.get("ADD_LINE");
	//-------------------------------------------------------------------------------------------//
	public static final String SET_CONVERTOBJ_H = Enc.get("CONVERT_OBJECT");
	//-------------------------------------------------------------------------------------------//
	public static final String SET_MOVEP_H = Enc.get("MOVE_OBJECT_POINT");
	public static final String SET_MOVESEG_H = Enc.get("MOVE_SEGMENT");
	public static final String SET_MOVEOBJ_H = Enc.get("MOVE_OBJECT");
	//-------------------------------------------------------------------------------------------//
	public static final String SET_ADDP2OBJ_H = Enc.get("ADD_FEATURE_POINT");
	public static final String SET_ADD2SEG_H = Enc.get("ADD_OBJECT_SEGMENT");
	//-------------------------------------------------------------------------------------------//
	public static final String MERGE_SEGS_H = Enc.get("MERGE_SEGMENTS_OBJECTS");
	public static final String SPLIT_SEGS_H = Enc.get("SEGMENT");
	//-------------------------------------------------------------------------------------------//
	public static final String SET_REMOVEP_H = Enc.get("DELETE_FEATURE_POINT");
	public static final String SET_REMSEG_H = Enc.get("DELETE_OBJECT_SEGMENT");
	public static final String SET_REMOVEOBJ_H = Enc.get("DELETE_OBJECT");
	//-------------------------------------------------------------------------------------------//
	public static final String COMMIT_H = Enc.get("SAVE_CHANGES?");
	public static final String ROLLBACK_H = Enc.get("REVERT_CHANGES");
	//-------------------------------------------------------------------------------------------//
	public static final String SET_FAST_EDITOR_ATTR = Enc.get("QUICK_ATTRIBUTE_EDIT");
	//-------------------------------------------------------------------------------------------//
	public static final String SET_PRESIZECRD_H = Enc.get("ENTER_COORDINATES");
	//-------------------------------------------------------------------------------------------//
	public static final String SET_STYLES_H = Enc.get("EDITOR_STYLE");
	//-------------------------------------------------------------------------------------------//
	public static final String SET_STANDARTMODE_H = Enc.get("SCROLL_MODE");
	protected SetStyleDlg styleDlg = null;
	protected ViewCoordinates viewcrd = null;
	protected IEditableGisObject newobj;
	protected MPoint startp;
	protected int indexp;
	protected boolean is_precisely_mode =false;
	int mode = NON_EDIT;//Режим редактирования
	JButton commitButton;
	JButton rollbackButton;
	private IViewControl mainmodule;//Ссылка на модуль
	private List<IBaseFilter> lfilter = new LinkedList<IBaseFilter>();//Фильтры (родительского слоя,слоя редактирования,фильтр родительских объектов редактирования)
	private IEditableStorage storage;//Редактируемое хранилище
	private List<ILayer> drweditlrs = new LinkedList<ILayer>();//Слой с помощью которого отображаются редактируемые объекты

	public Object init(Object obj) throws Exception
	{
		IParam attr = (IParam) obj;
		if (attr.getName().equalsIgnoreCase(KernelConst.VIEWCNTRL_TAGNAME))
			this.mainmodule = (IViewControl) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(KernelConst.FILTER_TAGNAME))
			lfilter.add((IBaseFilter) attr.getValue());
		else if (attr.getName().equalsIgnoreCase(KernelConst.STORAGE_TAGNAME))
			storage = ((IEditableStorage) attr.getValue());
		else if (attr.getName().equalsIgnoreCase(KernelConst.LAYER_TAGNAME))
			drweditlrs.add((ILayer) attr.getValue());
		return null;
	}

	public String getMenuName()
	{
		return Enc.get("EDITOR");
	}

	public JMenu addMenu(JMenu inmenu) throws Exception
	{
		if (inmenu.getComponentCount() > 0)
			inmenu.addSeparator();
		JMenuItem menuItem = new JMenuItem(SET_ADDPOINTOBJ_H, KeyEvent.VK_NONCONVERT);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setAddPObject();
			}
		});

		menuItem = new JMenuItem(SET_ADDLINEOBJ_H, KeyEvent.VK_NONCONVERT);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setAddLObject();
			}
		});

		inmenu.addSeparator();

		menuItem = new JMenuItem(SET_CONVERTOBJ_H, KeyEvent.VK_NONCONVERT);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setConvertObject();
			}
		});

		inmenu.addSeparator();

		menuItem = new JMenuItem(SET_MOVEP_H, KeyEvent.VK_NONCONVERT);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setMovePObject();
			}
		});

		menuItem = new JMenuItem(SET_MOVESEG_H, KeyEvent.VK_NONCONVERT);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setMoveSegment();
			}
		});

		menuItem = new JMenuItem(SET_MOVEOBJ_H, KeyEvent.VK_NONCONVERT);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setMoveObject();
			}
		});

		inmenu.addSeparator();

		menuItem = new JMenuItem(SET_ADDP2OBJ_H, KeyEvent.VK_NONCONVERT);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setAddP2Object();
			}
		});

		menuItem = new JMenuItem(SET_ADD2SEG_H, KeyEvent.VK_NONCONVERT);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setAddSeg2Object();
			}
		});

		inmenu.addSeparator();

		menuItem = new JMenuItem(MERGE_SEGS_H, KeyEvent.VK_NONCONVERT);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setMergeObject();
			}
		});

		menuItem = new JMenuItem(SPLIT_SEGS_H, KeyEvent.VK_NONCONVERT);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setSplitObject();
			}
		});

		inmenu.addSeparator();

		menuItem = new JMenuItem(SET_REMOVEP_H, KeyEvent.VK_NONCONVERT);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setRemovePObject();
			}
		});

		menuItem = new JMenuItem(SET_REMSEG_H, KeyEvent.VK_NONCONVERT);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setRemoveSegObject();
			}
		});

		menuItem = new JMenuItem(SET_REMOVEOBJ_H, KeyEvent.VK_NONCONVERT);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setRemoveObject();
			}
		});

		inmenu.addSeparator();

		menuItem = new JMenuItem(COMMIT_H, KeyEvent.VK_NONCONVERT);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					commit();
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
				mode = NON_EDIT;
			}
		});

		menuItem = new JMenuItem(ROLLBACK_H, KeyEvent.VK_NONCONVERT);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					rollback();
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
				mode = NON_EDIT;
			}
		});

		inmenu.addSeparator();

		menuItem = new JMenuItem(SET_FAST_EDITOR_ATTR, KeyEvent.VK_NONCONVERT);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				startFastEditor();
			}
		});

		inmenu.addSeparator();

		menuItem = new JMenuItem(SET_PRESIZECRD_H, KeyEvent.VK_NONCONVERT);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				showInCrdDialog();
			}
		});

		inmenu.addSeparator();

		menuItem = new JMenuItem(SET_STYLES_H, KeyEvent.VK_NONCONVERT);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				showStyleDlg();
			}
		});


		inmenu.addSeparator();

		menuItem = new JMenuItem(SET_STANDARTMODE_H, KeyEvent.VK_NONCONVERT);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setSatndartMode();
			}
		});

		return inmenu;
	}

	private void showStyleDlg()
	{
		if (styleDlg != null)
			styleDlg.setVisible(false);

		styleDlg = new SetStyleDlg(mainmodule, drweditlrs.get(0));//TODO Сделать возможно список правил
		styleDlg.pack();
		styleDlg.setVisible(true);
	}

	private void showInCrdDialog()
	{
		{
			if (viewcrd != null)
				viewcrd.setVisible(false);

			viewcrd = new ViewCoordinates(this, Enc.get("ENTER_COORDINATES"));
			viewcrd.pack();
			viewcrd.setVisible(true);
		}
	}

	public JPopupMenu addPopUpMenu(JPopupMenu inmenu) throws Exception
	{
		return inmenu;
	}

	public void registerListeners(JComponent component) throws Exception
	{
		component.addKeyListener(new KeyListener()
		{

			public void keyTyped(KeyEvent e)
			{
			}

			public void keyPressed(KeyEvent e)
			{
				try
				{

					if (e.getKeyCode() == KeyEvent.VK_S)
						is_precisely_mode =true;
					else
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					{
						switch (mode)
						{
							case ADD_IN_P_OBJECT:
							{
								if (newobj != null)
									newobj.removePoint(indexp);
								mode = ADD_P_OBJECT;
								resetEditor();
							}
							break;
						}
					}

				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}

			public void keyReleased(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_S)
					is_precisely_mode =false;
			}
		});
		component.addMouseMotionListener(new MouseMotionListener()
		{

			public void mouseDragged(MouseEvent e)
			{
			}

			public void mouseMoved(MouseEvent e)
			{
				if (!e.isConsumed())
				{
					try
					{
						MPoint dstoint_cur = mainmodule.getViewPort().getCopyConverter().getPointByDstPoint(e.getPoint());
						setDstPoint(dstoint_cur, startp);


						switch (mode)
						{
							case IN_LINE:
							case ADD_IN_P_OBJECT:
							case ADD_IN_P_SEGMENT_OBJECT:
							case MOVE_IN_OBJECT:
							case MOVE_IN_SEGMENT_OBJECT:
							case MOVE_IN_P_OBJECT:
								if (viewcrd != null && viewcrd.isShowing() && newobj != null && indexp >= 0)
									viewcrd.setPCoordinates(newobj.getPoint(indexp));
								else if (viewcrd != null && viewcrd.isShowing())
									viewcrd.resetPCoordinates();
								break;
							default:
								if (viewcrd != null && viewcrd.isShowing())
									viewcrd.resetPCoordinates();
						}
					}
					catch (Exception e1)
					{
						e1.printStackTrace();
					}
				}
			}
		});
		component.addMouseListener(new MouseListener()
		{
			public void mouseClicked(MouseEvent e)
			{
				try
				{
					if (!e.isConsumed())
					{
						if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3)
						{
							switch (mode)
							{
								case MERGE_IN_OBJECT:
								{
									mode = MERGE_OBJECT;
									resetEditor();
								}
								break;
								case ADD_IN_P_OBJECT:
								{
									mode = ADD_P_OBJECT;
									resetEditor();
								}
								break;
								case MOVE_IN_P_OBJECT:
								{
									if (is_precisely_mode && newobj!=null && indexp>=0)
									{
										MPoint rv=setSelObjects(newobj.getPoint(indexp), new HashSet<String>());
										if (rv!=null)
											newobj.setPoint(indexp,rv);
									}
									mode = MOVE_P_OBJECT;
									resetEditor();
								}
								break;
								case MOVE_IN_SEGMENT_OBJECT:
								{
									mode = MOVE_SEGMENT_OBJECT;
									resetEditor();
								}
								break;
								case REMOVE_IN_P_OBJECT:
								case SPLIT_IN_OBJECT:
								case REMOVE_IN_SEGMENT_OBJECT:
								case REMOVE_IN_OBJECT:
								case CONV_IN_LINE:
								{

									IProjConverter converter = mainmodule.getViewPort().getCopyConverter();
									Point drwpoint = e.getPoint();
									MPoint l_startp = converter.getPointByDstPoint(drwpoint);
									int l_indexp = newobj.getNearestIndexByPoint(l_startp);
									Point drwchp = converter.getDstPointByPoint(newobj.getPoint(l_indexp));
									double dx = drwchp.x - drwpoint.x;
									double dy = drwchp.y - drwpoint.y;
									double lds = dx * dx + dy * dy;
									if (lds <= 13)
									{
										indexp = l_indexp;
										startp = l_startp;
										if (mode == REMOVE_IN_P_OBJECT)
										{
											newobj.removePoint(indexp);
											mode = REMOVE_P_OBJECT;
										}
										else if (mode == SPLIT_IN_OBJECT)
										{
											newobj.splitCurveByPoint(indexp);
											mode = SPLIT_OBJECT;
										}
										else if (mode == REMOVE_IN_OBJECT)
										{
											mode = REMOVE_OBJECT;
											storage.removeObject(newobj.getCurveId());
										}
										else if (mode == REMOVE_IN_SEGMENT_OBJECT)
										{
											mode = REMOVE_SEGMENT_OBJECT;
											newobj.removeSegment(newobj.splitIndex(indexp).first);
										}
										else if (mode == CONV_IN_LINE)
										{
											mode = CONV_LINE;
											if (newobj != null)
											{
												if (newobj.getGeotype().equals(KernelConst.LINESTRING))
													newobj.setGeotype(KernelConst.LINEARRING);
												else if (newobj.getGeotype().equals(KernelConst.LINEARRING))
													newobj.setGeotype(KernelConst.LINESTRING);
												mainmodule.refresh(null);
											}
										}
										mainmodule.refresh(null);
										resetEditor();
									}
								}
								break;
								case MOVE_IN_OBJECT:
								{
									mode = MOVE_OBJECT;
									resetEditor();
								}
								break;
								case ADD_SEGMENT_OBJECT:
								case MOVE_P_OBJECT:
								case MOVE_SEGMENT_OBJECT:
								case MOVE_OBJECT:
								case ADD_P_OBJECT:
								case REMOVE_P_OBJECT:
								case REMOVE_SEGMENT_OBJECT:
								case REMOVE_OBJECT:
								case MERGE_OBJECT:
								case SPLIT_OBJECT:
								case CONV_LINE:
								{
									//Поднять диалог с объектами и выбрать из него объект
									//(для этого обратиться с фильтром в переданное хранилище)
									//Установить ближайшую точку объекта к курсору и сохранить ее индекс

									//TODO Потом внедрить диалог
									Point drwpoint = e.getPoint();

									MRect drawrect = new MRect(new MPoint(drwpoint.x - 5, drwpoint.y - 5),
											new MPoint(drwpoint.x + 5, drwpoint.y + 5));
									IProjConverter converter = mainmodule.getViewPort().getCopyConverter();
									MRect proj_rect = converter.getRectByDstRect(drawrect, null);
									Iterator<IBaseGisObject> it = storage.filterObjs(new DefMBBFilter(proj_rect));
									startp = converter.getPointByDstPoint(drwpoint);


									double ds = -1;
									//Фильтр обеспечивает нам отбор объектов у которых пересекаются зоны MBB
									//Далее проверим что бы расстояние до точки ближайшего из них было менее 10 пиксейлей
									while (it.hasNext())
									{
										IEditableGisObject l_newobj = storage.getEditableObject(it.next().getCurveId());
										if (l_newobj == null || (mode == ADD_P_OBJECT && l_newobj.getGeotype().equals(KernelConst.POINT)))
											continue;

										int l_indexp = l_newobj.getNearestIndexByPoint(startp);
										Point drwchp = converter.getDstPointByPoint(l_newobj.getPoint(l_indexp));

										double dx = drwchp.x - drwpoint.x;
										double dy = drwchp.y - drwpoint.y;
										double lds = dx * dx + dy * dy;
										if (ds < 0 || ds > lds)
										{
											ds = lds;
											if (ds < 256)
											{
												indexp = l_indexp;
												newobj = l_newobj;
											}
										}
									}

									if (newobj == null)  //Объект не найден: выходим
									{
										startp = null;
										indexp = -1;
									}
									else
									{
										setViewFilters(newobj);//Установим систему отображения для показа редактируемого объекта

										if (mode == MOVE_OBJECT)
											mode = MOVE_IN_OBJECT;
										else if (mode == MOVE_SEGMENT_OBJECT)
											mode = MOVE_IN_SEGMENT_OBJECT;
										else if (mode == MOVE_P_OBJECT)
											mode = MOVE_IN_P_OBJECT;
										else if (mode == ADD_SEGMENT_OBJECT)
											mode = ADD_IN_SEGMENT_OBJECT;
										else if (mode == REMOVE_P_OBJECT)
											mode = REMOVE_IN_P_OBJECT;
										else if (mode == SPLIT_OBJECT)
											mode = SPLIT_IN_OBJECT;
										else if (mode == CONV_LINE)
											mode = CONV_IN_LINE;
										else if (mode == MERGE_OBJECT)
										{
											Pair<Integer, Integer> pr = newobj.splitIndex(indexp);
											if (pr.first < newobj.getSegsNumbers() - 1)
											{//Проверка того что мы выбрали не последний сегмент
												mode = MERGE_IN_OBJECT;
												indexp = newobj.mergeSegments(pr.first, pr.first + 1, startp);
											}
										}
										else if (mode == REMOVE_SEGMENT_OBJECT)
											mode = REMOVE_IN_SEGMENT_OBJECT;
										else if (mode == REMOVE_OBJECT)
											mode = REMOVE_IN_OBJECT;
										else if (mode == ADD_P_OBJECT)
										{
											mode = ADD_IN_P_OBJECT;
											//Создать и вставить точку
											if (e.isShiftDown())
												indexp++;//Если кнопка нажата тогда добавить точку после индекса а не перед ним (дает возможность наращивания сегмента)
											newobj.addPoint(indexp, startp);
										}
										mainmodule.refresh(null);
									}
								}
								break;
								case ADD_IN_SEGMENT_OBJECT:
								{
									//получим координату точки
									MPoint dstoint = mainmodule.getViewPort().getCopyConverter().getPointByDstPoint(e.getPoint());
									newobj.addPoint(-1, dstoint);
									if (!KernelConst.POINT.equals(newobj.getGeotype()))
									{
										newobj.addPoint(indexp = newobj.numberOfPoints(), dstoint);
										mode = ADD_IN_P_SEGMENT_OBJECT;
									}
									else
										mode = ADD_SEGMENT_OBJECT;

								}
								break;
								case IN_LINE:
								case ADD_IN_P_SEGMENT_OBJECT:
								{
									if (e.isAltDown())
									{
										if (mode == IN_LINE)
										{
											if (newobj!=null && indexp>=0 && is_precisely_mode)
											{
												MPoint rv=setSelObjects(newobj.getPoint(indexp), new HashSet<String>());
												if (rv!=null)
													newobj.setPoint(indexp,rv);
											}
											mode = ADD_LINE;
										}
										else
											mode = ADD_SEGMENT_OBJECT;
										mainmodule.refresh(null);
									}
									else
									{
										MPoint dstoint = mainmodule.getViewPort().getCopyConverter().getPointByDstPoint(e.getPoint());
										MPoint rv=dstoint;
										if (newobj!=null && indexp>=0 && is_precisely_mode)
										{
											MPoint l_rv=setSelObjects(newobj.getPoint(indexp), new HashSet<String>());
											if (l_rv!=null)
												newobj.setPoint(indexp,rv=l_rv);
										}
										if (e.isShiftDown() && mode == IN_LINE) //Режим ввода следующей линии для сопряжения концов линии
											addObject(rv,e, KernelConst.LINESTRING);
										else
											newobj.addPoint(indexp = newobj.numberOfPoints(), dstoint);
										mainmodule.refresh(new CommandBean(KernelConst.P_SKIPDRAWWM, null, 0, ""));
									}
								}
								break;
								case ADD_POINT:
								case ADD_LINE:
								{
									MPoint rv=null;
									if (is_precisely_mode)
									{
										MPoint dstoint = mainmodule.getViewPort().getCopyConverter().getPointByDstPoint(e.getPoint());
										rv=setSelObjects(dstoint, new HashSet<String>());
									}
									if (mode==ADD_LINE)
									{
										addObject(rv,e, KernelConst.LINESTRING);
										mode = IN_LINE;
									}
									else
										addObject(rv,e, KernelConst.POINT);
									mainmodule.refresh(new CommandBean(KernelConst.P_SKIPDRAWWM, null, 0, ""));
								}
								break;
							}
							e.consume();
						}
					}
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}

			public void mousePressed(MouseEvent e)
			{
			}

			public void mouseReleased(MouseEvent e)
			{
			}

			public void mouseEntered(MouseEvent e)
			{
			}

			public void mouseExited(MouseEvent e)
			{
			}
		});
	}

	public void syncPointWithDstPoint(Pair<String, String> drawx2y, Pair<String, String> projx2y, boolean draw2proj) throws Exception
	{
		double dX = 0;
		double dY = 0;
		MPoint dstoint;

		if (draw2proj)
		{
			if (drawx2y.first.length() > 0 && drawx2y.second.length() > 0)
			{
				try
				{
					dX = Double.parseDouble(drawx2y.first);
					dY = Double.parseDouble(drawx2y.second);
				}
				catch (NumberFormatException e)
				{
					e.printStackTrace();
					return;
				}


				dstoint = mainmodule.getViewPort().getCopyConverter().getPointByDstPoint(new MPoint(dX, dY));
				projx2y.first = String.valueOf(dstoint.x);
				projx2y.second = String.valueOf(dstoint.y);
			}
		}
		else
		{
			if (projx2y.first.length() > 0 && projx2y.second.length() > 0)
			{
				try
				{
					dX = Double.parseDouble(projx2y.first);
					dY = Double.parseDouble(projx2y.second);
				}
				catch (NumberFormatException e)
				{
					e.printStackTrace();
					return;
				}

				Point point = mainmodule.getViewPort().getCopyConverter().getDstPointByPoint(new MPoint(dX, dY));
				drawx2y.first = String.valueOf(point.x);
				drawx2y.second = String.valueOf(point.y);
			}
		}
	}

	public void setDstPointByProjPair(Pair<String, String> x2y) throws Exception
	{
		if (newobj != null && indexp >= 0)
		{
			double dX = 0;
			double dY = 0;
			try
			{
				dX = Double.parseDouble(x2y.first);
				dY = Double.parseDouble(x2y.second);
			}
			catch (NumberFormatException e)
			{
				e.printStackTrace();
				return;
			}
			MPoint dstoint = new MPoint(dX, dY);
			setDstPoint(dstoint, new MPoint(startp));
			if (mode == MOVE_IN_P_OBJECT)
			{//Для этого режима у нас в setDstPoint накапливаются изменения в точке, поэтому мы его дорабатываем здесь
				newobj.setPoint(indexp, dstoint);
				mainmodule.refresh(new CommandBean(KernelConst.P_SKIPDRAWWM, null, 0, ""));//Передать команду спец. отрисовки
			}
			else if (startp != null)
				startp.setXY(dstoint);
		}
	}

	/**
	 *
	 * @param projp - точка интереса относительно которой производится анализ
	 * @param excludes - объекты ктороые не надо анализировать
	 * @return - ближайшая точка выбранного объекта
	 * @throws Exception -
	 */
	private MPoint setSelObjects(MPoint projp, Collection<String> excludes) throws Exception
	{
		if (newobj != null)
		{
			String pid;
			excludes.add(newobj.getCurveId());
			if ((pid = newobj.getParentId()) != null)
				excludes.add(pid);

		}

		IProjConverter converter = mainmodule.getViewPort().getCopyConverter();

		MPoint drwpoint = new MPoint(converter.getDstPointByPointD(projp));
		MRect drawrect = new MRect(new MPoint(drwpoint.x - 5, drwpoint.y - 5),
				new MPoint(drwpoint.x + 5, drwpoint.y + 5));

		MRect proj_rect = converter.getRectByDstRect(drawrect, null);
		Iterator<IBaseGisObject> it = storage.filterObjs(new DefMBBFilter(proj_rect));


		double ds = -1;
		MPoint rv=null;

		//Фильтр обеспечивает нам отбор объектов у которых пересекаются зоны MBB
		//Далее найдем ближайший к точке объект но не далее чем 10 пикселей
		excludes.addAll(storage.getNotCommited());

		IGisObject nearestobj = null;
		while (it.hasNext())
		{

			IGisObject l_obj = (IGisObject) it.next();
			if (l_obj == null || excludes.contains(l_obj.getCurveId()))
				continue;

			MPoint np = l_obj.getNearestBoundPointByPoint(projp);
			if (np==null)
				System.out.println("allert");
			Point drwchp = converter.getDstPointByPoint(np);

			double dx = drwchp.x - drwpoint.x;
			double dy = drwchp.y - drwpoint.y;
			double lds = dx * dx + dy * dy;

			if (lds <= 13 && (ds < 0 || ds > lds))
			{
				ds = lds;
				nearestobj = l_obj;
				rv=np;
			}
		}

		DefSelFilterByKeys bf = getSelFilter();
		if (bf != null)
			bf.clearKeySet();

		if (bf != null && nearestobj != null)
		{
//			System.out.println("lds:" + nearestobj.getCurveId());
			bf.addKey2Set(nearestobj.getCurveId());
		}
		return rv;
	}

	private DefSelFilterByKeys getSelFilter()
	{
		ILayer sellr = drweditlrs.get(1);
		List<IBaseFilter> filters = sellr.getFilters();
		for (IBaseFilter filter : filters)
			if (filter instanceof DefSelFilterByKeys)
				return (DefSelFilterByKeys) filter;
		return null;
	}

	private void setDstPoint(MPoint dstoint, MPoint startp)
			throws Exception
	{

		switch (mode)
		{
			case ADD_POINT:
			case ADD_LINE:
			    setSelObjects(dstoint, new HashSet<String>());
				mainmodule.refresh(new CommandBean(KernelConst.P_SKIPDRAWWM, null, 0, ""));//Передать команду спец. отрисовки
			break;

			case IN_LINE:
			case ADD_IN_P_OBJECT:
			case ADD_IN_P_SEGMENT_OBJECT:
			case MERGE_IN_OBJECT:
			{
				//получим последний сегмент объекта и отредактируем его
				if (mode == IN_LINE)
				{
					int ilastpoint = newobj.numberOfPoints() - 1;
					newobj.setPoint(ilastpoint, dstoint);//Установить точку объекта
				}
				else
					newobj.setPoint(indexp, dstoint);

				setSelObjects(dstoint, new HashSet<String>());
				mainmodule.refresh(new CommandBean(KernelConst.P_SKIPDRAWWM, null, 0, ""));//Передать команду спец. отрисовки
			}
			break;
			case MOVE_IN_P_OBJECT:
			{
				newobj.add2Point(indexp, new MPoint(dstoint.x - startp.x, dstoint.y - startp.y));
				setSelObjects(newobj.getPoint(indexp), new HashSet<String>());
				mainmodule.refresh(new CommandBean(KernelConst.P_SKIPDRAWWM, null, 0, ""));//Передать команду спец. отрисовки
			}
			break;
			case MOVE_IN_OBJECT:
			case MOVE_IN_SEGMENT_OBJECT:
			{
				if (mode == MOVE_IN_OBJECT)
					newobj.add2AllCoordinates(new MPoint(dstoint.x - startp.x, dstoint.y - startp.y));
				else
					newobj.add2SegCoordinates(newobj.splitIndex(indexp).first, new MPoint(dstoint.x - startp.x, dstoint.y - startp.y));
				mainmodule.refresh(new CommandBean(KernelConst.P_SKIPDRAWWM, null, 0, ""));//Передать команду спец. отрисовки
			}
			break;
		}

		if (startp != null)
			startp.setXY(dstoint);
	}

	private void setViewFilters(IEditableGisObject editableGisObject)
			throws Exception
	{
		((IKeySelFilter) lfilter.get(0)).addKey2Set(editableGisObject.getCurveId());
		((IKeySelFilter) lfilter.get(1)).addKey2Set(editableGisObject.getCurveId());
		if (editableGisObject.getParentId() != null)
			((IKeySelFilter) lfilter.get(2)).addKey2Set(editableGisObject.getParentId());
	}

	private void addObject(MPoint fstpnt,MouseEvent e, String geotype)
			throws Exception
	{
		//получим координату точки
		MPoint dstoint = mainmodule.getViewPort().getCopyConverter().getPointByDstPoint(e.getPoint());
		if (fstpnt==null)
			fstpnt=dstoint;

		newobj = storage.createObject(geotype);

		newobj.addPoint(-1, fstpnt);
		if (!KernelConst.POINT.equals(geotype))
			newobj.addPoint(1, dstoint);
		indexp = newobj.numberOfPoints()-1;
		setViewFilters(newobj);
	}

	public JToolBar addInToolBar(JToolBar systemtoolbar) throws Exception
	{
		if (systemtoolbar.getComponentCount() > 0)
			systemtoolbar.addSeparator();

		systemtoolbar.addSeparator();
		JButton button = null;
		{

			button = new JButton();
			ImageIcon addp = ImgResources.getIconByName("images/db_set_breakpoint.png", "AddPoint");
			if (addp != null)
				button.setIcon(addp);

			button.setToolTipText(SET_ADDPOINTOBJ_H);//TODO воспользоваться конвертором для имен
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setAddPObject();
				}
			});
			button.setMargin(new Insets(0, 0, 0, 0));
			systemtoolbar.add(button);
		}


		{
			button = new JButton();
			ImageIcon addline = ImgResources.getIconByName("images/viewBreakpoints.png", "AddPoint");
			if (addline != null)
				button.setIcon(addline);

			button.setToolTipText(SET_ADDLINEOBJ_H);//TODO воспользоваться конвертором для имен
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setAddLObject();
				}
			});

			button.setMargin(new Insets(0, 0, 0, 0));
			systemtoolbar.add(button);


			button.setMargin(new Insets(0, 0, 0, 0));
			systemtoolbar.add(button);
		}

		systemtoolbar.addSeparator();
		{
			ImageIcon convertico = ImgResources.getIconByName("images/sync.png", "convert");
			JButton convertbutton = new JButton();
			if (convertico != null)
				convertbutton.setIcon(convertico);

			convertbutton.setToolTipText(SET_CONVERTOBJ_H);//TODO воспользоваться конвертором для имен
			convertbutton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setConvertObject();
				}
			});

			convertbutton.setMargin(new Insets(0, 0, 0, 0));
			systemtoolbar.add(convertbutton);
		}

		systemtoolbar.addSeparator();

		{
			ImageIcon movp = ImgResources.getIconByName("images/persistenceRelationship.png", "movp");
			button = new JButton();
			if (movp != null)
				button.setIcon(movp);

			button.setToolTipText(SET_MOVEP_H);//TODO воспользоваться конвертором для имен
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setMovePObject();

				}
			});

			button.setMargin(new Insets(0, 0, 0, 0));
			systemtoolbar.add(button);
		}

		{
			ImageIcon movsegment = ImgResources.getIconByName("images/deploy.png", "movsegment");

			button = new JButton();
			if (movsegment != null)
				button.setIcon(movsegment);

			button.setToolTipText(SET_MOVESEG_H);//TODO воспользоваться конвертором для имен
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setMoveSegment();
				}
			});

			button.setMargin(new Insets(0, 0, 0, 0));
			systemtoolbar.add(button);
		}

		{
			ImageIcon movobj = ImgResources.getIconByName("images/order.png", "movobj");
			button = new JButton();
			if (movobj != null)
				button.setIcon(movobj);

			button.setToolTipText(SET_MOVEOBJ_H);//TODO воспользоваться конвертором для имен
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setMoveObject();
				}
			});

			button.setMargin(new Insets(0, 0, 0, 0));
			systemtoolbar.add(button);
		}


		systemtoolbar.addSeparator();
		{
			ImageIcon addpinobj = ImgResources.getIconByName("images/subtypes.png", "addpinobj");
			button = new JButton();
			if (addpinobj != null)
				button.setIcon(addpinobj);

			button.setToolTipText(SET_ADDP2OBJ_H);//TODO воспользоваться конвертором для имен
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setAddP2Object();
				}
			});

			button.setMargin(new Insets(0, 0, 0, 0));
			systemtoolbar.add(button);
		}

		{
			ImageIcon addsegment = ImgResources.getIconByName("images/class.png", "addsegment");


			button = new JButton();
			if (addsegment != null)
				button.setIcon(addsegment);

			button.setToolTipText(SET_ADD2SEG_H);//TODO воспользоваться конвертором для имен
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setAddSeg2Object();
				}
			});

			button.setMargin(new Insets(0, 0, 0, 0));
			systemtoolbar.add(button);
		}

		systemtoolbar.addSeparator();

		{
			ImageIcon mergeico = ImgResources.getIconByName("images/merge.png", "mergeico");
			button = new JButton();
			if (mergeico != null)
				button.setIcon(mergeico);

			button.setToolTipText(MERGE_SEGS_H);//TODO воспользоваться конвертором для имен
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setMergeObject();
				}
			});

			button.setMargin(new Insets(0, 0, 0, 0));
			systemtoolbar.add(button);
		}

		{
			ImageIcon splitobjico = ImgResources.getIconByName("images/split.png", "splitobj");
			button = new JButton();
			if (splitobjico != null)
				button.setIcon(splitobjico);

			button.setToolTipText(SPLIT_SEGS_H);//TODO воспользоваться конвертором для имен
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setSplitObject();
				}
			});

			button.setMargin(new Insets(0, 0, 0, 0));
			systemtoolbar.add(button);
		}

		systemtoolbar.addSeparator();

		{
			ImageIcon delpinco = ImgResources.getIconByName("images/breakpoint.png", "splitobj");
			button = new JButton();
			if (delpinco != null)
				button.setIcon(delpinco);

			button.setToolTipText(SET_REMOVEP_H);//TODO воспользоваться конвертором для имен
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setRemovePObject();
				}
			});

			button.setMargin(new Insets(0, 0, 0, 0));
			systemtoolbar.add(button);
		}

		{
			ImageIcon delpinco = ImgResources.getIconByName("images/suspend.png", "splitobj");
			button = new JButton();
			if (delpinco != null)
				button.setIcon(delpinco);

			button.setToolTipText(SET_REMSEG_H);//TODO воспользоваться конвертором для имен
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setRemoveSegObject();
				}
			});

			button.setMargin(new Insets(0, 0, 0, 0));
			systemtoolbar.add(button);
		}

		{
			ImageIcon delpinco = ImgResources.getIconByName("images/lightning.png", "splitobj");
			button = new JButton();
			if (delpinco != null)
				button.setIcon(delpinco);

			button.setToolTipText(SET_REMOVEOBJ_H);//TODO воспользоваться конвертором для имен
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setRemoveObject();
				}
			});

			button.setMargin(new Insets(0, 0, 0, 0));
			systemtoolbar.add(button);
		}


		systemtoolbar.addSeparator();
		{
			ImageIcon saveico = ImgResources.getIconByName("images/er-state.png", "save");
			commitButton = new JButton();
			if (saveico != null)
				commitButton.setIcon(saveico);

			commitButton.setToolTipText(COMMIT_H);//TODO воспользоваться конвертором для имен
			commitButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						commit();
					}
					catch (Exception e1)
					{
						e1.printStackTrace();
					}
					mode = NON_EDIT;
				}
			});
			commitButton.setMargin(new Insets(0, 0, 0, 0));
			systemtoolbar.add(commitButton);
		}

		{
			ImageIcon roolbackico = ImgResources.getIconByName("images/cancel.png", "roolback");
			rollbackButton = new JButton();
			if (roolbackico != null)
				rollbackButton.setIcon(roolbackico);

			rollbackButton.setToolTipText(ROLLBACK_H);//TODO воспользоваться конвертором для имен
			rollbackButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{

					try
					{
						rollback();
					}
					catch (Exception e1)
					{
						e1.printStackTrace();
					}
					mode = NON_EDIT;
				}
			});
			rollbackButton.setMargin(new Insets(0, 0, 0, 0));
			systemtoolbar.add(rollbackButton);
		}

		systemtoolbar.addSeparator();

		{
			ImageIcon editico = ImgResources.getIconByName("images/editSource.png", "editattr");
			button = new JButton();
			if (editico != null)
				button.setIcon(editico);

			button.setToolTipText(SET_FAST_EDITOR_ATTR);//TODO воспользоваться конвертором для имен
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					startFastEditor();
				}
			});

			button.setMargin(new Insets(0, 0, 0, 0));
			systemtoolbar.add(button);
		}

		systemtoolbar.addSeparator();

		{
			ImageIcon editcrd = ImgResources.getIconByName("images/uiForm.png", "editcrd");
			button = new JButton();
			if (editcrd != null)
				button.setIcon(editcrd);

			button.setToolTipText(SET_PRESIZECRD_H);//TODO воспользоваться конвертором для имен
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					showInCrdDialog();
				}
			});

			button.setMargin(new Insets(0, 0, 0, 0));
			systemtoolbar.add(button);
		}

		systemtoolbar.addSeparator();

		{
			ImageIcon editcrd = ImgResources.getIconByName("images/Options.png", "options");
			button = new JButton();
			if (editcrd != null)
				button.setIcon(editcrd);

			button.setToolTipText(SET_STYLES_H);//TODO воспользоваться конвертором для имен
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					showStyleDlg();
				}
			});

			button.setMargin(new Insets(0, 0, 0, 0));
			systemtoolbar.add(button);
		}

		systemtoolbar.addSeparator();

		{
			ImageIcon scrollico = ImgResources.getIconByName("images/homeFolder.png", "scrollico");
			button = new JButton();
			if (scrollico != null)
				button.setIcon(scrollico);

			button.setToolTipText(SET_STANDARTMODE_H);//TODO воспользоваться конвертором для имен
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setSatndartMode();
				}
			});

			button.setMargin(new Insets(0, 0, 0, 0));
			systemtoolbar.add(button);
		}
		return systemtoolbar;
	}

	private void setSatndartMode()
	{
		mode = NON_EDIT;
		resetEditor();
	}

	private void startFastEditor()
	{
		setSatndartMode();
		try
		{
			if (storage.notcommited())
			{
				Object[] options = {Enc.get("VERIFY"), Enc.get("ROLLBACK")};
				int ch = JOptionPane.showOptionDialog(null, Enc.get("VERIFY")+"?", Enc.get("THERE_ARE_UNCONFIRMED_ITEMS"),
						JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
				if (ch == 0)
					commit();
				else
					rollback();
			}
			String attrname = JOptionPane.showInputDialog(Enc.get("ENTER_ATTRIBUTE_NAME"), "CURVE_NAME");
			if (attrname != null && attrname.length() > 0)
			{
				InAttribute dialog = new InAttribute(attrname, storage, this);
				dialog.pack();
				dialog.setModal(false);
				dialog.setVisible(true);
			}
			else
				JOptionPane.showMessageDialog(null, Enc.get("ATTRIBUTE_NAME_CANNOT_BE_EMPTY"), Enc.get("ERROR"), JOptionPane.ERROR_MESSAGE);
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
	}

	private void setRemoveObject()
	{
		mode = REMOVE_OBJECT;
		resetEditor();
	}

	private void setRemoveSegObject()
	{
		mode = REMOVE_SEGMENT_OBJECT;
		resetEditor();
	}

	private void setRemovePObject()
	{
		mode = REMOVE_P_OBJECT;
		resetEditor();
	}

	private void setSplitObject()
	{
		mode = SPLIT_OBJECT;
		resetEditor();
	}

	private void setMergeObject()
	{
		mode = MERGE_OBJECT;
		resetEditor();
	}

	private void setAddSeg2Object()
	{
		mode = ADD_SEGMENT_OBJECT;
		resetEditor();
	}

	private void setAddP2Object()
	{
		mode = ADD_P_OBJECT;
		resetEditor();
	}

	private void setMoveObject()
	{
		mode = MOVE_OBJECT;
		resetEditor();
	}

	private void setMoveSegment()
	{
		mode = MOVE_SEGMENT_OBJECT;
		resetEditor();
	}

	private void setMovePObject()
	{
		mode = MOVE_P_OBJECT;
		resetEditor();
	}

	private void setConvertObject()
	{
		mode = CONV_LINE;
	}

	private void setAddLObject()
	{
		mode = ADD_LINE;
		resetEditor();
	}

	private void setAddPObject()
	{
		mode = ADD_POINT;
		resetEditor();
	}

	public void rollback()
			throws Exception
	{
		storage.rollback();
		for (IBaseFilter iBaseFilter : lfilter)
			((IKeySelFilter) iBaseFilter).clearKeySet();
		getSelFilter().clearKeySet();
		resetEditor();
	}

	public void commit()
			throws Exception
	{
		storage.commit();
		for (IBaseFilter iBaseFilter : lfilter)
			((IKeySelFilter) iBaseFilter).clearKeySet();
		getSelFilter().clearKeySet();
		resetEditor();
	}

	private void resetEditor()
	{
		try
		{
			newobj = null;
			startp = null;
			indexp = -1;
			mainmodule.refresh(null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void paintMe(Graphics g) throws Exception
	{
		for (ILayer drweditlr : drweditlrs)
			drweditlr.paintLayer(g, mainmodule.getViewPort());
	}

	@Override
	public boolean shouldIRepaint() throws Exception {
		return false;
	}

	public String getModuleName()
	{
		return MODULENAME;
	}

	public IAnswerBean execute(ICommandBean cmd) throws Exception
	{
		throw new UnsupportedOperationException();
	}

	public void setSelectedByCurveId(String curveId) throws Exception
	{
		rollback();
		IEditableGisObject giso = storage.getEditableObject(curveId);
		IProjConverter converter = mainmodule.getViewPort().getCopyConverter();
		Point[] drwpnts = new DefGisOperations().getCentralDrawPoints(converter, giso);
		Point olddrwpnt = mainmodule.getViewPort().getDrawSize();
		((IProjConverter) (converter)).getAsShiftConverter().recalcBindPointByDrawDxDy(
				new double[]{drwpnts[0].x - olddrwpnt.x / 2, drwpnts[0].y - olddrwpnt.y / 2});
		//Установить фильтры для показа редактируемых объектов
		mainmodule.getViewPort().setCopyConverter(converter);

		setViewFilters(giso);
		mainmodule.refresh(null);
	}

	public void registerNameConverter(INameConverter nameConverter, IFactory<INameConverter> factory) throws Exception
	{
		INameConverter l_nameConverter = null;
		if (factory != null && (l_nameConverter = factory.createByTypeName(MODULENAME)) != null)
			nameConverter.addNameConverter(l_nameConverter);
	}

	public void unload()
	{
	}
}
