import CircularProgress from 'material-ui/CircularProgress';
import * as React from 'react';
import {TableCellProps, TableCellRenderer} from 'react-virtualized';
import {colors} from '../../app/colors';
import {Predicate, Styled} from '../../types/Types';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';
import {Row} from '../layouts/row/Row';

const loadingStyle: React.CSSProperties = {
  marginTop: 100,
  marginBottom: 100,
};

export const LoadingLarge = withCssStyles(({cssStyles: {primary}}: ThemeContext) => (
  <CircularProgress
    size={60}
    style={loadingStyle}
    thickness={4}
    color={primary.bg}
  />
));

export const LoadingSmall = withCssStyles(({cssStyles: {primary}, style}: Styled & ThemeContext) => (
  <CircularProgress
    size={24}
    style={{marginBottom: 4, ...style}}
    thickness={2}
    color={primary.bg}
  />
));

const widgetLoadingStyle: React.CSSProperties = {
  marginTop: 30,
  marginBottom: 30,
  height: 58,
};

export const LoadingWidget = withCssStyles(({cssStyles: {primary}}: ThemeContext) => (
  <CircularProgress
    size={24}
    thickness={2}
    style={widgetLoadingStyle}
    color={primary.bg}
  />
));

const height = 14;

const loaderStyle: React.CSSProperties = {
  borderRadius: height / 2,
  backgroundColor: colors.dividerColor,
  height,
  width: '100%',
};

export const renderLoadingOr = (hasItem: Predicate<number>, cellRenderer: TableCellRenderer) =>
  (props: TableCellProps) => hasItem(props.rowIndex)
    ? cellRenderer(props)
    : (
      <Row style={props.columnIndex === 0 ? {paddingLeft: 14} : {}}>
        <div style={loaderStyle}/>
      </Row>
    );
