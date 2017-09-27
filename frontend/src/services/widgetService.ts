import {ColoredBoxModel} from '../usecases/widget/models/ColoredBoxModel';
import {DonutGraphModel} from '../usecases/widget/models/DonutGraphModel';
import {WidgetModel} from '../usecases/widget/models/WidgetModel';

export function widgetFactory(properties: DonutGraphModel | ColoredBoxModel): WidgetModel {
  let widget: WidgetModel;

  if (properties.hasOwnProperty('state')) {
    widget = new ColoredBoxModel();
  } else if (properties.hasOwnProperty('records')) {
    widget = new DonutGraphModel();
  } else {
    throw new TypeError('Could not instantiate a widget based on given properties');
  }
  Object.keys(properties).map(key => widget[key] = properties[key]);
  return widget;
}
