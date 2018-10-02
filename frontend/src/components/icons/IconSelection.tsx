import IconButton from 'material-ui/IconButton';
import * as React from 'react';
import {iconSizeLarge, iconStyle} from '../../app/themes';
import selectionIcon from '../../assets/images/selection-icon.svg';
import './Icons.scss';

export const selectionIconStyle: React.CSSProperties = {
  ...iconStyle,
  width: 40,
  height: 40,
  borderRadius: 20,
};

export const IconSelection = () => (
  <IconButton
    className="IconButton IconSelection"
    style={selectionIconStyle}
    iconStyle={iconSizeLarge}
  >
    <img src={selectionIcon}/>
  </IconButton>
);
