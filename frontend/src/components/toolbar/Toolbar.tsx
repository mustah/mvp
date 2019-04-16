import * as React from 'react';
import {WithChildren} from '../../types/Types';
import {Row, RowMiddle, RowRight, RowSpaceBetween} from '../layouts/row/Row';
import './Toolbar.scss';

export const Toolbar = ({children}: WithChildren) => (
  <RowSpaceBetween className="Toolbar">
    {children}
  </RowSpaceBetween>
);

export const ToolbarViewSettings = ({children}: WithChildren) => (
  <RowMiddle className="Toolbar-ViewSettings">
    {children}
  </RowMiddle>
);

export const ToolbarLeftPane = ({children}: WithChildren) => (
  <Row className="Toolbar-LeftPane">
    {children}
  </Row>
);

export const ToolbarRightPane = ({children}: WithChildren) => (
  <RowRight className="Toolbar-RightPane">
    {children}
  </RowRight>
);
