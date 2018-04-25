update measurement set quantity = 'Forward temperature'
where quantity = 'Flow' and dimension(value) = '1 K';
