import {Color} from '../../app/colors';
import {RequestsHttp} from '../../state/domain-models/domainModels';

export interface Colors {
  primary: Color;
  secondary: Color;
}

export interface ThemeState extends RequestsHttp {
  color: Colors;
}

export interface ThemeRequestModel {
  key: keyof Colors;
  value: Color;
}
