import kivy
kivy.require('2.3.0')
from kivy.app import App
from kivy.uix.label import Label
from kivy.uix.gridlayout import GridLayout

from curency import get_exchange_rate

class ExchangeRates(GridLayout):
    def __init__(self, curencies, **kwargs):
        super().__init__(**kwargs)
        self.cols = 2
        for curency in curencies:
            self.add_widget(Label(text=f'{curency.name}'))
            self.add_widget(Label(text=f'{curency.vunit_rate}'))

class RateApp(App):
    def build(self):
        try:
            curency_names = ['USD', 'EUR', 'AED', 'CNY']
            return ExchangeRates(get_exchange_rate(curency_names))
        except Exception as e:
            return Label(text=f"Ошибка: {e}")


if __name__ == '__main__':
    RateApp().run()