/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */
package edu.ucar.unidata.rosetta.domain;


import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Style class to format ToStringBuilder toString() for pretty printing in the transaction log file.
 */
public class TransactionLogStyle extends ToStringStyle {


    public static final TransactionLogStyle EMBEDDED_OBJECT_STYLE = new EmbeddedObjectStyle();
    public static final TransactionLogStyle NESTED_EMBEDDED_OBJECT_STYLE = new NestedEmbeddedObjectStyle();

    /**
     * Used to print outer/top-level objects.
     */
    public TransactionLogStyle() {
        super();
        super.setUseShortClassName(true);
        super.setUseIdentityHashCode(false);
        this.setContentStart("[");
        this.setFieldSeparator("\n  ");
        this.setFieldSeparatorAtStart(true);
        this.setContentEnd("\n]");
    }

    /**
     * Used to print nested objects.
     */
    private static final class EmbeddedObjectStyle extends TransactionLogStyle {

        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * Use the static constant rather than instantiating.
         */
        EmbeddedObjectStyle() {
            super();
            super.setUseShortClassName(true);
            super.setUseIdentityHashCode(false);
            this.setContentStart("[");
            this.setFieldSeparator("\n      ");
            this.setFieldSeparatorAtStart(true);
            this.setContentEnd("\n    ]");
        }


        /**
         * Custom method to append to the toString the class name.
         *
         * @param buffer  the StringBuffer to populate
         * @param object  the Object whose name to output
         */

        protected void appendClassName(final StringBuffer buffer, final Object object) {
            if (object != null) {
                buffer.append("\n    " + getShortClassName(object.getClass()));
            }
        }

        /**
         * Ensure Singleton after serialization.
         *
         * @return the singleton
         */
        private Object readResolve() {
            return TransactionLogStyle.EMBEDDED_OBJECT_STYLE;
        }

    }

    /**
     * Used to print inner-most nested objects.
     */
    private static final class NestedEmbeddedObjectStyle extends TransactionLogStyle {

        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * Use the static constant rather than instantiating.
         */
        NestedEmbeddedObjectStyle() {
            super();
            super.setUseShortClassName(true);
            super.setUseIdentityHashCode(false);
            this.setContentStart("[");
            this.setFieldSeparator("\n          ");
            this.setFieldSeparatorAtStart(true);
            this.setContentEnd("\n        ]");
        }


        /**
         * Custom method to append to the toString the class name.
         *
         * @param buffer  the StringBuffer to populate
         * @param object  the Object whose name to output
         */

        protected void appendClassName(final StringBuffer buffer, final Object object) {
            if (object != null) {
                buffer.append("\n        " + getShortClassName(object.getClass()));
            }
        }

        /**
         * Ensure Singleton after serialization.
         *
         * @return the singleton
         */
        private Object readResolve() {
            return TransactionLogStyle.NESTED_EMBEDDED_OBJECT_STYLE;
        }

    }


}
