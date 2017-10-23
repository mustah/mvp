import 'Icons.scss';
import IconButton from 'material-ui/IconButton';
import ActionSearch from 'material-ui/svg-icons/action/search';
import * as React from 'react';
import {iconSize} from '../../../app/themes';

export const SelectionIconButton = () => (
  <IconButton
    className="IconButton"
    style={{padding: 0, width: 30, height: 30}}
    iconStyle={iconSize.large}
  >
    <ActionSearch/>
  </IconButton>
);
