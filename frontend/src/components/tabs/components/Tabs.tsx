import {default as classNames} from 'classnames';
import * as React from 'react';
import {style} from 'typestyle';
import {ThemeContext, withCssStyles} from '../../hoc/withThemeProvider';
import {Column} from '../../layouts/column/Column';
import {TabContentProps} from './TabContent';
import './Tabs.scss';
import {TabTopBarProps} from './TabTopBar';

type TabsChildren = TabTopBarProps | TabContentProps;

interface TabsProps extends ThemeContext {
  children: Array<React.ReactElement<TabsChildren>>;
  className?: string;
}

export const Tabs = withCssStyles(({children, className, cssStyles: {primary}}: TabsProps) => {
  const tabsClassName = style({
    $nest: {
      '.Tab .Tab-header': {color: primary.fg},
      '.Tab:hover .TabUnderline': {borderTop: `5px solid ${primary.bgHover}`},
      '.Tab.isSelected:hover .TabUnderline': {borderTop: `5px solid ${primary.bg}`},
      '.TabUnderline.isSelected': {borderTop: `5px solid ${primary.bg}`},
    }
  });
  return (
    <Column className={classNames('Tabs', className, tabsClassName)}>
      {children}
    </Column>
  );
});
