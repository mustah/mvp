import 'Icons.scss';
import IconButton from 'material-ui/IconButton';
import ContentFilterList from 'material-ui/svg-icons/content/filter-list';
import * as React from 'react';
import {Clickable} from '../../../../types/Types';

const style = {
  padding: 2,
  width: 30,
  height: 30,
};

export const IconFilter = (props: Clickable) => (
  <IconButton
    style={style}
    onClick={props.onClick}
  >
    <ContentFilterList/>
  </IconButton>
);
