import 'Icons.scss';
import IconButton from 'material-ui/IconButton';
import NavigationArrowBack from 'material-ui/svg-icons/navigation/arrow-back';
import * as React from 'react';
import {Clickable} from '../../../../types/Types';
import {iconSizeLarge, iconStyle} from '../../../app/themes';

export const IconNavigationBack = (props: Clickable) => (
  <IconButton
    className="IconButton"
    style={iconStyle}
    iconStyle={iconSizeLarge}
    onClick={props.onClick}
  >
    <NavigationArrowBack/>
  </IconButton>
);
