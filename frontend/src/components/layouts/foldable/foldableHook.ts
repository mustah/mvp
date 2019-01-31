import * as React from 'react';
import {Callback, Visible} from '../../../types/Types';

interface Props extends Visible {
  showHide: Callback;
}

export const useToggleVisibility = (initialState: boolean): Props => {
  const [isVisible, toggle] = React.useState<boolean>(initialState);
  const showHide = () => toggle(!isVisible);
  return {isVisible, showHide};
};
