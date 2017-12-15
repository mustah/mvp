import FlatButton from 'material-ui/FlatButton';
import * as React from 'react';
import {translate} from '../../services/translationService';
import './ButtonClose.scss';
import {OnClick} from '../../types/Types';

interface ButtonCloseProps {
  onClick: OnClick;
}

export const ButtonClose = ({onClick}: ButtonCloseProps) => (
  <FlatButton
    label={translate('close')}
    onClick={onClick}
    className="FlatButton"
  />
);
