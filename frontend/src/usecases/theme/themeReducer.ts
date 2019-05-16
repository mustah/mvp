import {ActionType, getType} from 'typesafe-actions';
import {Color} from '../../app/colors';
import {changePrimaryColor, changeSecondaryColor, resetColors} from './themeActions';

export interface Colors {
  primary: Color;
  secondary: Color;
}

export interface ThemeState {
  color: Colors;
}

export const initialState: ThemeState = {
  color: {
    primary: '#0091ea',
    secondary: '#b6e2cc',
  }
};

type ActionTypes = ActionType<typeof changePrimaryColor | typeof changeSecondaryColor | typeof resetColors>;

export const theme = (state: ThemeState = initialState, action: ActionTypes): ThemeState => {
  switch (action.type) {
    case getType(changePrimaryColor):
      return {...state, color: {...state.color, primary: action.payload}};
    case getType(changeSecondaryColor):
      return {...state, color: {...state.color, secondary: action.payload}};
    case getType(resetColors):
      return initialState;
    default:
      return state;
  }
};
