import {makeDataFormatter} from '../domainModelSchema';
import {Widget} from './widgetModels';

export const widgetDataFormatter = makeDataFormatter<Widget>('widgets');
