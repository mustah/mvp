import {Layout} from 'react-grid-layout';
import {Identifiable} from '../../../types/Types';

interface WidgetLayout {
  layout: Layout[];
}

export interface Dashboard extends Identifiable {
  layout: WidgetLayout;
}
