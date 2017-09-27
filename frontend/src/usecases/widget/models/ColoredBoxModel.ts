import {State} from '../../../types/Types';
import {WidgetModel} from './WidgetModel';

export class ColoredBoxModel extends WidgetModel {
  state: State;
  subtitle: string;
  value: string;
  /**
   * Quantity is something measured, like "energy", "water"
   */
  // quantity?: string;
  /**
   * Unit is what we are measuring the value in, like "kWh", "m^3"
   */
  unit: string;
}
