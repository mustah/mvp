import FlatButton from 'material-ui/FlatButton';
import * as React from 'react';
import {translate} from '../../services/translationService';
import './DialogButtons.scss';
import {OnClick} from '../../types/Types';
import * as classNames from 'classnames';

interface DialogButtonProps {
  onClick: OnClick;
  disabled?: boolean;
}

export const ButtonClose = ({onClick}: DialogButtonProps) => (
  <FlatButton
    label={translate('close')}
    onClick={onClick}
    className="FlatButton"
  />
);

export const ButtonConfirm = ({onClick, disabled}: DialogButtonProps) => (
  <FlatButton
    label={translate('confirm')}
    onClick={onClick}
    disabled={disabled}
    className={classNames('FlatButton', {disabled})}
  />
  );

export const ButtonCancel = ({onClick}: DialogButtonProps) => (
  <FlatButton
    label={translate('cancel')}
    onClick={onClick}
    className="FlatButton"
  />
  );
