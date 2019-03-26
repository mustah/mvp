import {default as classNames} from 'classnames';
import FlatButton from 'material-ui/FlatButton';
import IconButton from 'material-ui/IconButton';
import ContentClear from 'material-ui/svg-icons/content/clear';
import * as React from 'react';
import {colors} from '../../app/themes';
import {translate} from '../../services/translationService';
import {OnClick} from '../../types/Types';
import './DialogButtons.scss';

interface DialogButtonProps {
  onClick: OnClick;
  disabled?: boolean;
}

const closeButtonStyle: React.CSSProperties = {position: 'absolute', right: 8, top: 8};
const iconStyle: React.CSSProperties = {width: 28, height: 28};

export const ButtonClose = ({onClick}: DialogButtonProps) => (
  <IconButton
    iconStyle={iconStyle}
    onClick={onClick}
    style={closeButtonStyle}
  >
    <ContentClear color={colors.lightBlack} hoverColor={colors.iconHover}/>
  </IconButton>
);

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
