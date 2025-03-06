package com.example.vibecut.CustomizeProject;

public  class CustomLayoutHelper {
    private static BaseCustomLineLayout currentVisibleHandlesLayout = null;
    public static void updateHandlesVisibility(BaseCustomLineLayout newLayout) {

        if (currentVisibleHandlesLayout != null && currentVisibleHandlesLayout != newLayout) {
            currentVisibleHandlesLayout.setHandlesVisibility(false); // Скрываем рамки у предыдущего элемента
        }
        currentVisibleHandlesLayout = newLayout; // Обновляем текущий элемент
    }

}
