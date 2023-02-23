package ru.ts.toykernel.plugins.gissearch;

import ru.ts.toykernel.attrs.IAttrs;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.toykernel.consts.INameConverter;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.gui.IViewControl;
import ru.ts.toykernel.gui.apps.SFFacilities;
import su.mwlib.utils.Enc;
import su.org.imglab.clengine.ViewImage;
import su.org.imglab.utils.ImageFiles;

import java.util.*;

public class FGisSearch extends GisSearch{
    public FGisSearch(IViewControl mainmodule) throws Exception {
        super(mainmodule);
    }

    public FGisSearch() throws Exception {
    }

    protected boolean viewSelectedObjectInDetail(IBaseGisObject drawMe) throws Exception {
        if (drawMe != null)
        {
            String str_images = null;
            INameConverter storNmWithCodeNm = getStorNmWithCodeNm(mainmodule.getProjContext());
            IAttrs objAttrs = drawMe.getObjAttrs();

            IDefAttr iDefAttr = objAttrs.get(storNmWithCodeNm.codeAttrNm2StorAttrNm(KernelConst.ATTR_IMG_REF));
            if (iDefAttr != null && iDefAttr.getValue() != null)
                str_images = iDefAttr.getValue().toString();
            if (str_images != null) {
                String[] imageNames = ImageFiles.getImageNames(str_images);

                IDefAttr nameAttr = objAttrs.get(storNmWithCodeNm.codeAttrNm2StorAttrNm(attrasname));
                String tsname = null;
                if (nameAttr != null) {
                    Object value = nameAttr.getValue();
                    if (value != null)
                        tsname = value.toString();
                }
                if (tsname == null)
                    tsname = "";


                ViewImage dialog = new ViewImage(Enc.get("PROPERTY_PHOTOS")+" " + tsname, img_path, imageNames);
                if (dialog.isImageReady()) {
                    dialog.pack();
                    dialog.setVisible(true);
                    return true;
                }
            }
        }
        return false;
    }

    protected Set<String> getObjectsIdByObjectName(String currentName)
    {
        Set<String> selIxs = super.getObjectsIdByObjectName(currentName);
        Map<String, List<String>> selIndices = new HashMap<String, List<String>>();
        for (String  lr2obj: selIxs)
        {
            String[] pr=lr2obj.split("#\\$");
            if (pr!=null && pr.length>=2)
            {
                String key = pr[0];
                List<String> ll = selIndices.get(key);
                if (ll==null)
                    selIndices.put(key,ll=new LinkedList<String>());
                ll.add(pr[1]);
            }
        }

        ((SFFacilities)(mainmodule.getApplication())).setSelectByIndices(selIndices);
        return selIxs;
    }
}
