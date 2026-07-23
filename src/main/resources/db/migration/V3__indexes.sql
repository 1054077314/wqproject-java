-- Secondary indexes for common list / join paths

CREATE INDEX idx_tokens_user_id ON tokens (user_id);
CREATE INDEX idx_tokens_expires_at ON tokens (expires_at);
CREATE INDEX idx_product_images_product_id ON product_images (product_id);
CREATE INDEX idx_comments_product_id ON comments (product_id);
CREATE INDEX idx_appointments_product_id ON appointments (product_id);
CREATE INDEX idx_appointments_buyer_id ON appointments (buyer_id);
CREATE INDEX idx_favorites_user_id ON favorites (user_id);
CREATE INDEX idx_products_seller_id ON products (seller_id);
CREATE INDEX idx_products_category_id ON products (category_id);
