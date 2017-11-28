import FlatButton from 'material-ui/FlatButton';
import * as React from 'react';
import {translate} from '../../services/translationService';
import './ButtonClose.scss';

type Callback = (event: any) => any;

interface ButtonCloseProps {
  onClick: Callback;
}

export const ButtonClose = (props: ButtonCloseProps) => (
  <FlatButton
    label={translate('close')}
    onClick={props.onClick}
    className="FlatButton"
  />
);
