import {FlatButton, FlatButtonProps} from 'material-ui';
import * as React from 'react';

interface Props {
  page: number;
  key: string;
}

export const PageNumberButton = ({disabled, onClick, key, page}: FlatButtonProps & Props) => (
  <FlatButton
    className="PageNumber"
    disabled={disabled}
    onClick={onClick}
    key={`pagination-${key}`}
  >
    {page}
  </FlatButton>
);
