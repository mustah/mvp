import {State} from '../../../types/Types';

export class WidgetModel {
  title: string;
  href: string;
};

export class DonutGraphModel extends WidgetModel {
  records: object[];
}

export class ColoredBoxModel extends WidgetModel {
  state: State;
  subtitle: string;
  value: string;
  /**
   * Quantity is something measured, like "energy", "water"
   */
  //quantity?: string;
  /**
   * Unit is what we are measuring the value in, like "kWh", "m^3"
   */
  unit: string;
}

export function widgetFactory(properties: WidgetModel[]): WidgetModel {
  let widget: WidgetModel;

  if (properties.hasOwnProperty('state')) {
    widget = new ColoredBoxModel();
  } else if(properties.hasOwnProperty('records')) {
    widget = new DonutGraphModel();
  } else {
    throw new TypeError("Could not instantiate a widget based on given properties");
  }
  Object.keys(properties).map(key => widget[key] = properties[key]);
  return widget;
}
