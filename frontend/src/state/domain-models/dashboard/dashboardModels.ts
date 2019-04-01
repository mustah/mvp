import {Layout} from 'react-grid-layout';
import {Identifiable} from '../../../types/Types';

export interface Dashboard extends Identifiable {
  layout: WidgetLayout;
}

export interface WidgetLayout {
  layout: Layout[];
}
