import {Layout} from 'react-grid-layout';
import {Identifiable} from '../../../types/Types';
import {Widget} from '../widget/WidgetModels';

export interface Dashboard extends Identifiable {
  layout: WidgetLayout;
  widgets?: Widget[];
}

export interface WidgetLayout {
  layout: Layout[];
}