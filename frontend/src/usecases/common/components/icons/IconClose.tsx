import 'Icons.scss';
import IconButton from 'material-ui/IconButton';
import NavigationClose from 'material-ui/svg-icons/navigation/close';
import * as React from 'react';
import {Clickable} from '../../../../types/Types';
import {iconSizeLarge, iconStyle} from '../../../app/themes';

export const IconClose = (props: Clickable) => (
  <IconButton
    className="IconButton"
    style={iconStyle}
    iconStyle={iconSizeLarge}
    onClick={props.onClick}
  >
    <NavigationClose/>
  </IconButton>
);
