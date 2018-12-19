package de.toomuchcoffee.lyricstagger.gui;

enum Step {
    START, READ_FILES, FIND_LYRICS, WRITE_LYRICS;

    public Step next() {
        switch (this) {
            case READ_FILES:
                return FIND_LYRICS;
            case FIND_LYRICS:
                return WRITE_LYRICS;
            case START:
            case WRITE_LYRICS:
            default:
                return READ_FILES;
        }
    }
}
