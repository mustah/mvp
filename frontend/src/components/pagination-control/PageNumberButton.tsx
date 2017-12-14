import {FlatButton, FlatButtonProps} from 'material-ui';
import * as React from 'react';

interface Props {
  page: number;
}

export const PageNumberButton = ({disabled, onClick, page}: FlatButtonProps & Props) => (
  <FlatButton
    className="PageNumber"
    disabled={disabled}
    onClick={onClick}
  >
    {page}
  </FlatButton>
);
