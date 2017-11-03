import 'Icons.scss';
import IconButton from 'material-ui/IconButton';
import ActionSearch from 'material-ui/svg-icons/action/search';
import * as React from 'react';
import {iconSizeLarge, iconStyle} from '../../../app/themes';

export const SelectionIconButton = () => (
  <IconButton
    className="IconButton"
    style={iconStyle}
    iconStyle={iconSizeLarge}
  >
    <ActionSearch/>
  </IconButton>
);
