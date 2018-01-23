import IconButton from 'material-ui/IconButton';
import ContentFilterList from 'material-ui/svg-icons/content/filter-list';
import * as React from 'react';
import {iconSizeLarge, iconStyle} from '../../app/themes';
import './Icons.scss';

export const IconSelection = () => (
  <IconButton
    className="IconButton"
    style={iconStyle}
    iconStyle={iconSizeLarge}
  >
    <ContentFilterList />
  </IconButton>
);
