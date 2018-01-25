import FlatButton from 'material-ui/FlatButton';
import * as React from 'react';
import {translate} from '../../services/translationService';
import './DialogButtons.scss';
import {OnClick} from '../../types/Types';

interface DialogButtonProps {
  onClick: OnClick;
}

export const ButtonClose = ({onClick}: DialogButtonProps) => (
  <FlatButton
    label={translate('close')}
    onClick={onClick}
    className="FlatButton"
  />
);

export const ButtonConfirm = ({onClick}: DialogButtonProps) => (
  <FlatButton
    label={translate('confirm')}
    onClick={onClick}
    className="FlatButton"
  />
  );

export const ButtonCancel = ({onClick}: DialogButtonProps) => (
  <FlatButton
    label={translate('cancel')}
    onClick={onClick}
    className="FlatButton"
  />
  );
