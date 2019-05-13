import FlatButton from 'material-ui/FlatButton';
import * as React from 'react';
import {translate} from '../../services/translationService';
import {OnClick} from '../../types/Types';

interface DialogButtonProps {
  onClick: OnClick;
  disabled?: boolean;
}

export const ButtonConfirm = ({onClick, disabled}: DialogButtonProps) => (
  <FlatButton primary={true} label={translate('confirm')} onClick={onClick} disabled={disabled}/>
);

export const ButtonCancel = ({onClick}: DialogButtonProps) => (
  <FlatButton secondary={true} label={translate('cancel')} onClick={onClick}/>
);
