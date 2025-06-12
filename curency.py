import requests
import xml.etree.ElementTree as ET

class Curency():
    def __init__(self, char_code, vunit_rate, name):
        self.char_code = char_code
        self.vunit_rate = vunit_rate
        self.name = name


def get_raw_exchange_rate():
    url = "https://www.cbr.ru/scripts/XML_daily.asp"
    response = requests.get(url)
    response.encoding = 'windows-1251'  # CBR возвращает XML в этой кодировке

    root = ET.fromstring(response.text)
    return root

def get_exchange_rate(char_codes):
    root = get_raw_exchange_rate()
    curency_list = []
    for valute in root.findall('Valute'):
        char_code = valute.find('CharCode').text
        if char_code in char_codes:
            vunit_rate = valute.find('VunitRate').text.replace(',', '.')
            name = valute.find('Name').text
            curency_list.append(Curency(char_code, vunit_rate, name))
    return curency_list
    

    

# Пример использования
char_codes = ['USD', 'EUR', 'AED', 'CNY']
curency_list = get_exchange_rate(char_codes)

for curency in curency_list:
    print(curency.name)
    print(curency.vunit_rate)
