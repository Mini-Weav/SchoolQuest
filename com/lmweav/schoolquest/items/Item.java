package com.lmweav.schoolquest.items;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.SparseArray;

import com.lmweav.schoolquest.GameActivity;
import com.lmweav.schoolquest.R;
import com.lmweav.schoolquest.utilities.TextBoxStructure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

import androidx.annotation.NonNull;

import static com.lmweav.schoolquest.Constants.*;
import static com.lmweav.schoolquest.Game.GAME;

/*
 * School Quest: Item
 * This class is used for in-game items.
 *
 * @author Luke Weaver
 * @version 1.0.9
 * @since 2019-10-16
 */
public class Item implements Comparable, Serializable {

    private static SparseArray<Item> items;

    private static Bitmap empty;

    private int id;

    private int buyPrice;
    private int sellPrice;

    private boolean keyItem;

    private String name;
    private String description;

    private transient Bitmap icon;
    private transient Bitmap menuIcon;

    private transient Runnable effect;

    private transient Paint paint = new Paint();


    /*---------------------------------------------------------------------------------------------
    | Constructors
    ----------------------------------------------------------------------------------------------*/

    public Item(Context context, int id, int buyPrice, int sellPrice, boolean keyItem, String name,
                String description, Bitmap icon, Runnable effect) {
        this.id = id;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.keyItem = keyItem;
        this.name = name;
        this.description = description;
        this.icon = icon;
        menuIcon = createMenuIcon(context);
        this.effect = effect;
    }

    /*---------------------------------------------------------------------------------------------
    | Getters and Setters
    ----------------------------------------------------------------------------------------------*/

    public static Item getItem(int i) { return items.get(i); }

    public static Bitmap getEmptyIcon() { return empty; }

    public int getId() { return id; }

    public int getBuyPrice() { return buyPrice; }

    public int getSellPrice() { return sellPrice; }

    public boolean isKeyItem() { return keyItem; }

    public String getName() { return name; }

    public String getDescription() { return description; }

    public Bitmap getIcon() { return icon; }

    public Bitmap getMenuIcon() { return menuIcon; }

    public Runnable getEffect() { return effect; }

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    private Bitmap createMenuIcon(Context context) {
        Bitmap bg;
        if (keyItem) {
            bg = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable._ui_main_inventory_key_bg);
        } else {
            bg = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable._ui_main_inventory_normal_bg);
        }

        Bitmap menuIcon = Bitmap.createBitmap(icon.getWidth(), icon.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas img = new Canvas(menuIcon);
        img.drawBitmap(bg, 0, 0, paint);
        img.drawBitmap(icon, 0, 0, paint);
        return menuIcon;
    }

    @Override
    public String toString() {
        return "[" + name + ": " + buyPrice + ", " + sellPrice + ", " + keyItem + "]\n\t" + "[" +
                description + "]";
    }

    public static void readItems(Context context) {
        empty = BitmapFactory.decodeResource(context.getResources(),
                R.drawable._ui_main_inventory_item_empty);
        InputStream inputStream = context.getResources().openRawResource(R.raw._items);

        items = new SparseArray<>();

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                Runnable effect = null;
                String[] split = line.replaceAll("(^.*?\\[|]\\s*$)", "").
                        split("]\\[");

                final String[] itemInfo = split[0].split(",");
                final int id = Integer.parseInt(itemInfo[0]);

                if (split.length > 2) {
                    final String[] effectInfo = split[2].split(",");
                    switch (effectInfo[0]) {
                        case "xp":
                            effect = new Runnable() {
                                @Override
                                public void run() {
                                    if (GAME.getPlayer().hasEaten()) {
                                        GameActivity.getInstance().displayTextBox(
                                                new TextBoxStructure("> You're not hungry now!"));
                                    } else {
                                        switch (Integer.parseInt(effectInfo[1])) {
                                            case 0:
                                                GAME.getPlayer().setBuff(DT_INDEX, 0);
                                                GAME.getPlayer().setBuff(FT_INDEX, 1);
                                                GameActivity.getInstance().displayTextBox(
                                                        new TextBoxStructure("> The use of pita " +
                                                                "bread in a pizza is inspiring! " +
                                                                "Actions take less time in DT and Food " +
                                                                "Tech now!"));
                                                break;
                                            case 1:
                                                GAME.getPlayer().setBuff(PE_INDEX, 0);
                                                GameActivity.getInstance().displayTextBox(
                                                        new TextBoxStructure("> The pasta has " +
                                                                "filled you with energy! " +
                                                                "Run time has increased and rest " +
                                                                "time has decreased in PE!"));
                                                break;
                                            case 2:
                                                GAME.getPlayer().setBuff(CHEMISTRY_INDEX, 0);
                                                GAME.getPlayer().setBuff(ICT_INDEX, 1);
                                                GameActivity.getInstance().displayTextBox(
                                                        new TextBoxStructure("> The omega-3-rich " +
                                                                "tuna boosts your brain power! " +
                                                                "Actions take less time in Chemistry and " +
                                                                "ICT now!"));
                                                break;
                                        }
                                        GAME.playSFX(SFX_BUFF);
                                        GAME.removeItem(Item.getItem(id));
                                        GAME.getPlayer().setEaten(true);
                                    }
                                }
                            };
                            break;
                        case "buff":
                            effect = new Runnable() {
                                @Override
                                public void run() {
                                    double rand = Math.random();
                                    if (GAME.getPlayer().hasEaten()) {
                                        GameActivity.getInstance().displayTextBox(
                                                new TextBoxStructure("> You're not hungry now!"));
                                    } else {
                                        if (rand < Double.parseDouble(effectInfo[1])) {
                                            GAME.playSFX(SFX_DEBUFF);
                                            GAME.getPlayer().setCondition(UNWELL_CONDITION);
                                            GameActivity.getInstance().displayTextBox(
                                                    new TextBoxStructure("> The food is bad! " +
                                                            "You don't feel very well..."));
                                        } else {
                                            GAME.playSFX(SFX_BUFF);
                                            GAME.getPlayer().setCondition(GREAT_CONDITION);
                                            GameActivity.getInstance().displayTextBox(
                                                    new TextBoxStructure("> The food is " +
                                                            "delicious! You feel great now!"));
                                        }
                                        GAME.removeItem(Item.getItem(id));
                                        GAME.getPlayer().setEaten(true);
                                    }
                                }
                            };
                            break;
                    }
                }

                Bitmap icon = null;

                switch (id) {
                    case DT_BOOK_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_book0);
                        break;
                    case FT_BOOK_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_book1);
                        break;
                    case PE_BOOK_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_book2);
                        break;
                    case CHEM_BOOK_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_book3);
                        break;
                    case ICT_BOOK_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_book4);
                        break;
                    case CANTEEN0_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_canteen0);
                        break;
                    case CANTEEN1_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_canteen1);
                        break;
                    case CANTEEN2_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_canteen2);
                        break;
                    case CRAFT_D_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_craft_d);
                        break;
                    case CRAFT_C_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_craft_c);
                        break;
                    case CRAFT_B_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_craft_b);
                        break;
                    case CRAFT_A_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_craft_a);
                        break;
                    case DRINK0_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_drink0);
                        break;
                    case DRINK1_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_drink1);
                        break;
                    case DRINK2_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_drink2);
                        break;
                    case FOOD_D_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_food_d);
                        break;
                    case FOOD_C_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_food_c);
                        break;
                    case FOOD_B_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_food_b);
                        break;
                    case FOOD_A_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_food_a);
                        break;
                    case KEY0_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_key0);
                        break;
                    case KEY1_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_key1);
                        break;
                    case KEY2_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_key2);
                        break;
                    case KEY3_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_key3);
                        break;
                    case KEY4_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_key4);
                        break;
                    case KEY5_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_key5);
                        break;
                    case KEY6_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_key4);
                        break;
                    case DT_SHEET_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_sheet0);
                        break;
                    case FT_SHEET_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_sheet1);
                        break;
                    case PE_SHEET_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_sheet2);
                        break;
                    case CHEM_SHEET_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_sheet3);
                        break;
                    case ICT_SHEET_INDEX:
                        icon = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable._ui_main_inventory_item_sheet4);
                        break;
                }

                items.put(Integer.parseInt(itemInfo[0]), new Item(context,
                        Integer.parseInt(itemInfo[0]), Integer.parseInt(itemInfo[2]),
                        Integer.parseInt(itemInfo[3]), Integer.parseInt(itemInfo[4]) == 1,
                        itemInfo[1], split[1], icon, effect));
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int compareTo(@NonNull Object o) {
        return this.id - ((Item) o).id;
    }
}
