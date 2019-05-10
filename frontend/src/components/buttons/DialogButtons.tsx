import {default as classNames} from 'classnames';
import FlatButton from 'material-ui/FlatButton';
import * as React from 'react';
import {translate} from '../../services/translationService';
import {OnClick} from '../../types/Types';

interface DialogButtonProps {
  onClick: OnClick;
  disabled?: boolean;
}

export const ButtonConfirm = ({onClick, disabled}: DialogButtonProps) => (
  <FlatButton
    label={translate('confirm')}
    primary={true}
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
