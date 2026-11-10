package com.kavyaraj.tillflow.data.local

import com.kavyaraj.tillflow.domain.model.Product

/** Demo catalogue for a small café, loaded on first launch so the app has something to show. */
internal val sampleProducts = listOf(
    Product("p01", "Flat White", "Coffee", 340, 50),
    Product("p02", "Cappuccino", "Coffee", 350, 50),
    Product("p03", "Americano", "Coffee", 290, 50),
    Product("p04", "Oat Latte", "Coffee", 380, 3),
    Product("p05", "English Breakfast Tea", "Tea", 250, 40),
    Product("p06", "Earl Grey", "Tea", 250, 0),
    Product("p07", "Chai Latte", "Tea", 360, 25),
    Product("p08", "Almond Croissant", "Bakery", 325, 12),
    Product("p09", "Cinnamon Bun", "Bakery", 350, 4),
    Product("p10", "Banana Bread", "Bakery", 295, 8),
    Product("p11", "Cheese & Ham Toastie", "Food", 595, 10),
    Product("p12", "Tomato Soup", "Food", 495, 6),
)
